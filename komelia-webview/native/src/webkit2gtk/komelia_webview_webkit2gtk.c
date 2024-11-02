#include "../komelia_webview.h"
#include "../komelia_callbacks.h"
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
// use static variable to use it inside callback
static request_interceptor *static_interceptor = NULL;
static gboolean scheme_is_registered = false;
static GMutex mutex;

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    GHashTable *bind_callbacks;
    request_interceptor *interceptor;
    GtkWidget *gtk_toplevel;
} webkit2gtk_data;

static char *extensions_dir = NULL;

static void komelia_uri_scheme_request_cb(WebKitURISchemeRequest *request, gpointer) {
    const gchar *uri = webkit_uri_scheme_request_get_uri(request);
    if (static_interceptor == NULL) {
        GError *error = g_error_new(WEBKIT_NETWORK_ERROR,
                                    WEBKIT_NETWORK_ERROR_FAILED,
                                    "interceptor is not initialized");
        webkit_uri_scheme_request_finish_error(request, error);
        g_error_free(error);
        return;
    }


    load_result_t *result = NULL;
    if (strncmp("komelia", uri, 7) == 0) {
        char *normalized_uri = malloc(sizeof(char) * (strlen(uri) + 10));
        strcpy(normalized_uri, "http");
        strcat(normalized_uri, uri + 7);
        result = komelia_interceptor_run(static_interceptor, normalized_uri);
        free(normalized_uri);
    } else {
        result = komelia_interceptor_run(static_interceptor, uri);
    }

    if (result == NULL) {
        GError *error = g_error_new(WEBKIT_NETWORK_ERROR, WEBKIT_NETWORK_ERROR_FAILED,
                                    "Invalid result");
        webkit_uri_scheme_request_finish_error(request, error);
        g_error_free(error);
        return;
    }


    GInputStream *stream = g_memory_input_stream_new_from_data(result->data, result->size, g_free);
    webkit_uri_scheme_request_finish(request, stream, result->size, result->content_type);

    g_object_unref(stream);
    free(result->content_type);
    free(result);
}

void reparent_awt_window(JNIEnv *env, webkit2gtk_data *webview_data, jobject awt_window) {
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

    XSync(x11_display, false);
    GdkWindow *gdk_window = gtk_widget_get_window(webview_data->gtk_toplevel);
    XID gtk_xid = gdk_x11_window_get_xid(gdk_window);
    XReparentWindow(x11_display, gtk_xid, awt_xid, 0, 0);
    XSync(x11_display, false);
}

static void
initialize_web_extensions(WebKitWebContext *context, gpointer) {
    if (extensions_dir == NULL) {
        const gchar *tmp_dir = g_get_tmp_dir();
        extensions_dir = malloc(sizeof(char) * (strlen(tmp_dir) + 32));
        strcpy(extensions_dir, tmp_dir);
        strcat(extensions_dir, "/komelia/libs/webkit");
    }

    webkit_web_context_set_web_extensions_directory(context, extensions_dir);
}

void komelia_register_request_interceptor(komelia_webview_t webview, request_interceptor *interceptor) {
    g_mutex_lock(&mutex);

    webkit2gtk_data *webview_data = webview;
    if (webview_data->interceptor != NULL) {
        komelia_interceptor_destroy(webview_data->interceptor);
    }
    webview_data->interceptor = interceptor;

    static_interceptor = interceptor;

    if (scheme_is_registered) {
        g_mutex_unlock(&mutex);
        return;
    }

    webkit_web_context_register_uri_scheme(
        webkit_web_context_get_default(),
        "komelia",
        komelia_uri_scheme_request_cb,
        NULL,
        NULL
    );

    webkit_web_context_register_uri_scheme(
        webkit_web_context_get_default(),
        "komelias",
        komelia_uri_scheme_request_cb,
        NULL,
        NULL
    );

    scheme_is_registered = true;
    g_mutex_unlock(&mutex);
}

komelia_webview_t komelia_webview_create(JNIEnv *env, jobject awt_window) {
    gtk_init_check(0, NULL);
    gdk_set_allowed_backends("x11");
    g_signal_connect(webkit_web_context_get_default (),
                     "initialize-web-extensions",
                     G_CALLBACK (initialize_web_extensions),
                     NULL);


    webkit2gtk_data *webview_data = malloc(sizeof(webkit2gtk_data));
    (*env)->GetJavaVM(env, &webview_data->jvm);
    webview_data->bind_callbacks = g_hash_table_new_full(g_str_hash,
                                                         g_str_equal,
                                                         NULL,
                                                         komelia_bind_callback_destroy);
    webview_data->interceptor = NULL;


    webview_data->gtk_toplevel = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_widget_show(webview_data->gtk_toplevel);

    webview_t webview = webview_create(0, webview_data->gtk_toplevel);
    reparent_awt_window(env, webview_data, awt_window);

    WebKitWebView *webkit_webview = webview_get_native_handle(webview, WEBVIEW_NATIVE_HANDLE_KIND_BROWSER_CONTROLLER);
    WebKitSettings *setting = webkit_web_view_get_settings(webkit_webview);
    webkit_settings_set_enable_developer_extras(setting, true);

    // WebKitWebInspector *inspector = webkit_web_view_get_inspector(webkit_webview);
    // webkit_web_inspector_show(WEBKIT_WEB_INSPECTOR(inspector));

    webview_data->webview = webview;
    return webview_data;
}

webview_t komelia_webview_get_webview(komelia_webview_t data) {
    return ((webkit2gtk_data *) data)->webview;
}

JavaVM *komelia_webview_get_jvm(komelia_webview_t data) {
    return ((webkit2gtk_data *) data)->jvm;
}

void komelia_webview_bind(komelia_webview_t webview, bind_callback_t *callback) {
    webkit2gtk_data *webview_data = webview;
    g_hash_table_insert(webview_data->bind_callbacks, callback->name_chars, callback);
    webview_bind(webview_data->webview, callback->name_chars, komelia_bind_callback_run, callback);
}

void komelia_destroy_callback(webview_t webview, void *data) {
    webkit2gtk_data *webview_data = data;
    if (webview_data->interceptor != NULL) {
        komelia_interceptor_destroy(webview_data->interceptor);
    }
    g_hash_table_destroy(webview_data->bind_callbacks);

    webview_terminate(webview);
    webview_destroy(webview);
    gtk_window_close(GTK_WINDOW(webview_data->gtk_toplevel));
    free(webview_data);
}

void komelia_webview_destroy(komelia_webview_t data) {
    webkit2gtk_data *webview_data = data;
    webview_dispatch(webview_data->webview, komelia_destroy_callback, data);
}
