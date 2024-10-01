#include "webview/webview.h"
#include <stddef.h>
#include <gtk/gtk.h>
#include <X11/X.h>
#include <gdk/gdkx.h>
#include <gdk/gdk.h>
#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>

#define EUnsatisfiedLink "java/lang/UnsatisfiedLinkError"
#define EError "java/lang/Error"
#define EIllegalState "java/lang/IllegalStateException"

static webview_t webview = NULL;

void throwByName(JNIEnv *env, const char *name, const char *msg) {
    jclass cls;

    (*env)->ExceptionClear(env);

    cls = (*env)->FindClass(env, name);

    if (cls != NULL) { /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);

        /* It's a good practice to clean up the local references. */
        (*env)->DeleteLocalRef(env, cls);
    }
}

JNIEXPORT void JNICALL
Java_snd_webview_JvmWebview_start(JNIEnv *env, jobject this, jobject window) {
    gtk_init(0, NULL);
    gdk_set_allowed_backends("x11");
    GtkWidget *gtk_window = gtk_window_new(GTK_WINDOW_TOPLEVEL);

    Display *x11_display = NULL;
    jlong awt_xid = 0;
    JAWT_DrawingSurface *ds;
    JAWT_DrawingSurfaceInfo *dsi;
    jint lock;
    JAWT awt;
    awt.version = JAWT_VERSION_9;

    if (!JAWT_GetAWT(env, &awt)) {
        throwByName(env, EUnsatisfiedLink, "Can't load JAWT");
        return;
    }

    ds = awt.GetDrawingSurface(env, window);
    if (ds == NULL) {
        throwByName(env, EError, "Can't get drawing surface");
        return;
    }

    lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        throwByName(env, EError, "Can't get drawing surface lock");
        return;
    }

    dsi = ds->GetDrawingSurfaceInfo(ds);
    if (dsi == NULL) {
        throwByName(env, EError, "Can't get drawing surface info");
    } else {
        JAWT_X11DrawingSurfaceInfo *xdsi = (JAWT_X11DrawingSurfaceInfo *) dsi->platformInfo;
        if (xdsi != NULL) {
            x11_display = xdsi->display;
            awt_xid = (long) xdsi->drawable;
        } else {
            throwByName(env, EError, "Can't get X11 platform info");
        }
        ds->FreeDrawingSurfaceInfo(dsi);
    }
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);

    if (x11_display == NULL) {
        throwByName(env, EIllegalState, "Can't get X11 display");
    }
    if (awt_xid == 0) {
        throwByName(env, EIllegalState, "Can't get Drawable");
    }

    fprintf(stderr, "awt xid %lu\n", awt_xid);

    gtk_widget_show(gtk_window);
    GdkWindow *gdk_window = gtk_widget_get_window(GTK_WIDGET(gtk_window));
    XID gtk_xid = gdk_x11_window_get_xid(gdk_window);
    fprintf(stderr, "gtk xid %lu\n", gtk_xid);

    XReparentWindow(x11_display, gtk_xid, awt_xid, 0, 0);

    XWindowAttributes awt_window_attributes;
    if (XGetWindowAttributes(x11_display, awt_xid, &awt_window_attributes)) {
        throwByName(env, EIllegalState, "Can't get awt window attributes");
    }

    webview = webview_create(0, gtk_window);
    webview_set_title(webview, "Basic Example");
    webview_set_size(webview, awt_window_attributes.width, awt_window_attributes.height, WEBVIEW_HINT_NONE);
    webview_set_html(webview, "Thanks for using webview!");
    webview_run(webview);
}

JNIEXPORT void JNICALL
Java_snd_webview_JvmWebview_updateSize(JNIEnv *env, jobject this, jint width, jint height) {
    webview_set_size(webview, width, height, WEBVIEW_HINT_NONE);
}
