#ifndef KOMELIA_WEBVIEW_H
#define KOMELIA_WEBVIEW_H

#include <jni.h>
#include "komelia_callbacks.h"
#include <glib.h>

typedef void *komelia_webview_t;

static inline void komelia_throw_jvm_exception(JNIEnv *env, const char *message) {
    jclass class = (*env)->FindClass(env, "snd/webview/WebviewException");
    (*env)->ThrowNew(env, class, message);
}

komelia_webview_t komelia_webview_create(JNIEnv *env, jobject awt_window);

void komelia_webview_destroy(komelia_webview_t data);

webview_t komelia_webview_get_webview(komelia_webview_t);

JavaVM *komelia_webview_get_jvm(komelia_webview_t);

void komelia_webview_bind(komelia_webview_t webview, bind_callback_t *callback);

void komelia_register_request_interceptor(komelia_webview_t webview, request_interceptor *interceptor);


#endif //KOMELIA_WEBVIEW_H
