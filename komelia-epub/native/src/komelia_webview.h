#ifndef KOMELIA_WEBVIEW_H
#define KOMELIA_WEBVIEW_H

#include <jni.h>
#include "komelia_callbacks.h"

static inline void komelia_throw_jvm_exception(JNIEnv *env, const char *message) {
    (*env)->ExceptionClear(env);
    jclass class = (*env)->FindClass(env, "snd/webview/WebviewException");
    (*env)->ThrowNew(env, class, message);
}

void komelia_set_parent_window(JNIEnv *env, webview_t webview, jobject awt_window);

webview_t komelia_webview_create();

void komelia_webview_destroy(webview_t webview);

void komelia_register_scheme_loader(char *scheme, resource_loader_t *handler);

#endif //KOMELIA_WEBVIEW_H
