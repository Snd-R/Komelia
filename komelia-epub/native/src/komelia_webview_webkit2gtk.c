#include "komelia_webview.h"
#include "komelia_callbacks.h"
#include <gtk/gtk.h>
#include <gdk/gdk.h>
#include <gdk/gdkx.h>
#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>
#include <X11/X.h>
#include <webkit2/webkit2.h>
#include <webview/webview.h>

// webview library initializes webkit webview with default context
// That context is stored in static variable and is reused when new webview is created
//
// there's no way to unregister uri handler
// registering uri scheme second time will cause error
// keep track of already registered handlers in static list
static GList *registered_schemes = NULL;
static GHashTable *scheme_handlers = NULL;
static GMutex scheme_mutex;

static void komelia_uri_scheme_request_cb(WebKitURISchemeRequest *request, gpointer) {
    g_mutex_lock(&scheme_mutex);
    const gchar *scheme = webkit_uri_scheme_request_get_scheme(request);
    const gchar *path = webkit_uri_scheme_request_get_path(request);


    if (scheme_handlers == NULL) {
        GError *error = g_error_new(WEBKIT_NETWORK_ERROR, WEBKIT_NETWORK_ERROR_FAILED,
                                    "resources handlers are not initialized");
        webkit_uri_scheme_request_finish_error(request, error);
        g_error_free(error);
        g_mutex_unlock(&scheme_mutex);
        return;
    }
    resource_loader_t *handler = g_hash_table_lookup(scheme_handlers, scheme);
    if (handler == NULL) {
        GError *error = g_error_new(WEBKIT_NETWORK_ERROR, WEBKIT_NETWORK_ERROR_FAILED,
                                    "uri scheme handler not found");
        webkit_uri_scheme_request_finish_error(request, error);
        g_error_free(error);
        g_mutex_unlock(&scheme_mutex);
        return;
    }

    load_result_t *result = komelia_load_resource(handler, path);
    if (result == NULL) {
        GError *error = g_error_new(WEBKIT_NETWORK_ERROR, WEBKIT_NETWORK_ERROR_FAILED,
                                    "Invalid result");
        webkit_uri_scheme_request_finish_error(request, error);
        g_error_free(error);
        g_mutex_unlock(&scheme_mutex);
        return;
    }

    GInputStream *stream = g_memory_input_stream_new_from_data(result->data, result->size, g_free);
    webkit_uri_scheme_request_finish(request, stream, result->size, NULL);

    g_object_unref(stream);
    free(result);
    g_mutex_unlock(&scheme_mutex);
}

GtkWidget *komelia_webview_get_toplevel_window(webview_t webview) {
    GtkWidget *webview_widget = webview_get_native_handle(webview, WEBVIEW_NATIVE_HANDLE_KIND_UI_WIDGET);
    return gtk_widget_get_toplevel(webview_widget);
}

void komelia_webview_destroy_callback(webview_t webview, void *data) {
    g_mutex_lock(&scheme_mutex);
    g_hash_table_destroy(scheme_handlers);
    scheme_handlers = NULL;

    GtkWidget *toplevel = komelia_webview_get_toplevel_window(webview);
    webview_terminate(webview);
    webview_destroy(webview);
    gtk_window_close(GTK_WINDOW(toplevel));

    g_mutex_unlock(&scheme_mutex);
}

void komelia_webview_destroy(webview_t webview) {
    webview_dispatch(webview, komelia_webview_destroy_callback,NULL);
}

webview_t komelia_webview_create() {
    g_mutex_lock(&scheme_mutex);
    if (scheme_handlers == NULL) {
        scheme_handlers = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, NULL);
    }

    gtk_init_check(0, NULL);
    gdk_set_allowed_backends("x11");

    GtkWidget *current_window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_widget_show(current_window);

    webview_t webview = webview_create(0, current_window);
    WebKitWebView *webkit_webview = webview_get_native_handle(
        webview, WEBVIEW_NATIVE_HANDLE_KIND_BROWSER_CONTROLLER);

    WebKitSettings *setting = webkit_web_view_get_settings(webkit_webview);
    webkit_settings_set_enable_developer_extras(setting, true);

    WebKitWebInspector *inspector = webkit_web_view_get_inspector(webkit_webview);
    webkit_web_inspector_show(WEBKIT_WEB_INSPECTOR(inspector));
    g_mutex_unlock(&scheme_mutex);
    return webview;
}

void komelia_set_parent_window(JNIEnv *env, webview_t webview, jobject awt_window) {
    Display *x11_display = NULL;
    jlong awt_xid = 0;
    JAWT_DrawingSurface *ds;
    JAWT_DrawingSurfaceInfo *dsi;
    jint lock;
    JAWT awt;
    awt.version = JAWT_VERSION_9;

    if (!JAWT_GetAWT(env, &awt)) {
        komelia_throw_jvm_exception(env, "Can't load JAWT");
        return;
    }

    ds = awt.GetDrawingSurface(env, awt_window);
    if (ds == NULL) {
        komelia_throw_jvm_exception(env, "Can't get drawing surface");
        return;
    }

    lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        komelia_throw_jvm_exception(env, "Can't get drawing surface lock");
        return;
    }

    dsi = ds->GetDrawingSurfaceInfo(ds);
    if (dsi == NULL) {
        komelia_throw_jvm_exception(env, "Can't get drawing surface info");
    } else {
        JAWT_X11DrawingSurfaceInfo *xdsi = dsi->platformInfo;
        if (xdsi != NULL) {
            x11_display = xdsi->display;
            awt_xid = (long) xdsi->drawable;
        } else {
            komelia_throw_jvm_exception(env, "Can't get X11 platform info");
        }
        ds->FreeDrawingSurfaceInfo(dsi);
    }
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);

    if (x11_display == NULL) {
        komelia_throw_jvm_exception(env, "Can't get X11 display");
    }
    if (awt_xid == 0) {
        komelia_throw_jvm_exception(env, "Can't get Drawable");
    }

    GtkWidget *gtk_toplevel = komelia_webview_get_toplevel_window(webview);
    GdkWindow *gdk_window = gtk_widget_get_window(gtk_toplevel);
    XID gtk_xid = gdk_x11_window_get_xid(gdk_window);
    fprintf(stderr, "awt xid %lu\n", awt_xid);
    fprintf(stderr, "gtk xid %lu\n", gtk_xid);


    XReparentWindow(x11_display, gtk_xid, awt_xid, 0, 0);
    XSync(x11_display, false);
}


void komelia_register_scheme_loader(char *scheme, resource_loader_t *handler) {
    g_mutex_lock(&scheme_mutex);
    GList *registered_scheme = g_list_find_custom(registered_schemes, scheme, (GCompareFunc) strcmp);
    if (registered_scheme == NULL) {
        char *static_scheme = g_strdup(scheme);
        webkit_web_context_register_uri_scheme(
            webkit_web_context_get_default(),
            static_scheme,
            komelia_uri_scheme_request_cb,
            NULL,
            NULL
        );

        GList *appended = g_list_append(registered_schemes, static_scheme);
        if (registered_schemes == NULL) {
            registered_schemes = appended;
        }
    }

    char *scheme_key = g_strdup(scheme);
    g_hash_table_insert(scheme_handlers, scheme_key, handler);
    g_mutex_unlock(&scheme_mutex);
}
