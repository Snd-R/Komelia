#include <stdlib.h>
#include <stdint.h>
#include <glib.h>
#include <webview/webview.h>
#include "komelia_webview.h"

komelia_webview_t *komelia_webview_from_jvm(JNIEnv *env, jobject jvm_webview) {
    jclass class = (*env)->GetObjectClass(env, jvm_webview);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    komelia_webview_t *webview = (komelia_webview_t *) (*env)->GetLongField(env, jvm_webview, ptr_field);

    if (webview == NULL) {
        komelia_throw_jvm_exception(env, "webview pointer is null\n");
        return NULL;
    }

    return webview;
}

void webview_dispatch_navigate(webview_t webview, void *uri) { webview_navigate(webview, uri); }

JNIEXPORT void JNICALL Java_snd_webview_Webview_navigate(JNIEnv *env, jobject this, jstring jvm_uri) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);

    const char *uri_chars = (*env)->GetStringUTFChars(env, jvm_uri, 0);
    char *uri = g_strdup(uri_chars);
    (*env)->ReleaseStringUTFChars(env, jvm_uri, uri_chars);
    webview_dispatch(webview, webview_dispatch_navigate, uri);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_updateSize(JNIEnv *env, jobject this, jint width, jint height) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);
    webview_set_size(webview, width, height, WEBVIEW_HINT_NONE);
}

void webview_dispatch_bind(webview_t webview, void *data) {
    bind_callback_t *callback = data;
    webview_bind(webview, callback->name_chars, komelia_bind_callback_run, callback);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bind(JNIEnv *env, jobject this, jstring name, jobject jvm_callback) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);
    bind_callback_t *callback = komelia_bind_callback_create(env, name, jvm_callback, webview);

    komelia_webview_bind(webview_data, callback);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bindReturn(JNIEnv *env, jobject this, jstring id, jstring result) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);
    const char *id_chars = (*env)->GetStringUTFChars(env, id, NULL);
    const char *result_chars = (*env)->GetStringUTFChars(env, result, NULL);

    webview_return(webview, id_chars, 0, result_chars);

    (*env)->ReleaseStringUTFChars(env, id, id_chars);
    (*env)->ReleaseStringUTFChars(env, id, result_chars);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bindReject(JNIEnv *env, jobject this, jstring id, jstring result) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);

    const char *id_chars = (*env)->GetStringUTFChars(env, id, NULL);
    const char *message = (*env)->GetStringUTFChars(env, result, NULL);
    webview_return(webview, id_chars, -1, message);

    (*env)->ReleaseStringUTFChars(env, id, id_chars);
    (*env)->ReleaseStringUTFChars(env, id, message);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_runMainLoop(JNIEnv *env, jobject this, jobject callback) {
    komelia_webview_t *komelia_webview = komelia_webview_from_jvm(env, this);

    main_started_callback_t *started_callback = malloc(sizeof(main_started_callback_t));
    started_callback->jvm = komelia_webview_get_jvm(komelia_webview);
    started_callback->object = (*env)->NewGlobalRef(env, callback);
    jclass runnable_class = (*env)->GetObjectClass(env, callback);
    started_callback->method = (*env)->GetMethodID(env, runnable_class, "run", "()V");

    webview_t webview = komelia_webview_get_webview(komelia_webview);
    webview_dispatch(webview, komelia_main_started_callback, started_callback);
    webview_run(webview);
}

JNIEXPORT jlong JNICALL
Java_snd_webview_Webview_create(JNIEnv *env, jobject this, jobject awt_window) {
    komelia_webview_t webview = komelia_webview_create(env, awt_window);
    if (webview == NULL) return 0;
    return (int64_t) webview;
}

JNIEXPORT void JNICALL
Java_snd_webview_Webview_registerRequestInterceptor(JNIEnv *env, jobject this, jobject jvm_interceptor) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_t webview = komelia_webview_get_webview(webview_data);

    request_interceptor *interceptor = komelia_interceptor_create(env, jvm_interceptor, webview);
    komelia_register_request_interceptor(webview_data, interceptor);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_destroy(JNIEnv *env, jclass this, jlong ptr) {
    komelia_webview_t *webview_data = (komelia_webview_t *) ptr;
    komelia_webview_destroy(webview_data);
}
