#include <stdlib.h>
#include <glib.h>
#include <webview/webview.h>
#include "komelia_webview.h"

typedef struct {
    webview_t webview;
    JavaVM *jvm;

    GHashTable *bind_callbacks;
    GHashTable *resource_loaders;
} komelia_webview_t;

komelia_webview_t *komelia_webview_from_jvm(JNIEnv *env, jobject jvm_webview) {
    jclass class = (*env)->GetObjectClass(env, jvm_webview);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    komelia_webview_t *image = (komelia_webview_t *) (*env)->GetLongField(env, jvm_webview, ptr_field);

    if (image == NULL) {
        komelia_throw_jvm_exception(env, "webview pointer is null\n");
        return NULL;
    }

    return image;
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_loadUri(JNIEnv *env, jobject this, jstring uri) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);

    const char *uri_chars = (*env)->GetStringUTFChars(env, uri, 0);
    webview_navigate(webview_data->webview, uri_chars);
    (*env)->ReleaseStringUTFChars(env, uri, uri_chars);
}

JNIEXPORT void JNICALL
Java_snd_webview_Webview_registerResourceLoadHandler(JNIEnv *env, jobject this,
                                                     jstring jvm_scheme, jobject jvm_handler) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);

    jclass class = (*env)->GetObjectClass(env, jvm_handler);
    jmethodID method = (*env)->GetMethodID(env, class, "run",
                                           "(Ljava/lang/String;)Lsnd/webview/ResourceLoadResult;");

    const char *scheme_chars = (*env)->GetStringUTFChars(env, jvm_scheme, 0);
    char *scheme = g_strdup(scheme_chars);
    (*env)->ReleaseStringUTFChars(env, jvm_scheme, scheme_chars);

    resource_loader_t *handler = malloc(sizeof(resource_loader_t));
    handler->jvm = webview_data->jvm;
    handler->webview = webview_data->webview;
    handler->object = (*env)->NewGlobalRef(env, jvm_handler);
    handler->method = method;
    handler->scheme = scheme;

    g_hash_table_insert(webview_data->resource_loaders, scheme, handler);

    komelia_register_scheme_loader(scheme, handler);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_updateSize(JNIEnv *env, jobject this, jint width, jint height) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_set_size(webview_data->webview, width, height, WEBVIEW_HINT_NONE);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bind(JNIEnv *env, jobject this, jstring name, jobject jvm_callback) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);

    jclass class = (*env)->GetObjectClass(env, jvm_callback);
    jmethodID method = (*env)->GetMethodID(env, class, "run", "(Ljava/lang/String;Ljava/lang/String;)V");

    bind_callback_t *callback = malloc(sizeof(bind_callback_t));
    callback->jvm = webview_data->jvm;
    callback->webview = webview_data->webview;
    callback->name = (*env)->NewGlobalRef(env, name);
    callback->object = (*env)->NewGlobalRef(env, jvm_callback);
    callback->method = method;

    const char *jvm_name_chars = (*env)->GetStringUTFChars(env, name, 0);
    char *name_key = g_strdup(jvm_name_chars);
    (*env)->ReleaseStringUTFChars(env, name, jvm_name_chars);

    g_hash_table_insert(webview_data->bind_callbacks, name_key, callback);

    webview_bind(webview_data->webview, name_key, komelia_bind_callback, callback);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bindReturn(JNIEnv *env, jobject this, jstring id, jstring result) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    const char *id_chars = (*env)->GetStringUTFChars(env, id, NULL);
    const char *result_chars = (*env)->GetStringUTFChars(env, result, NULL);

    webview_return(webview_data->webview, id_chars, 0, result_chars);

    (*env)->ReleaseStringUTFChars(env, id, id_chars);
    (*env)->ReleaseStringUTFChars(env, id, result_chars);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_bindReject(JNIEnv *env, jobject this, jstring id, jstring result) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);

    const char *id_chars = (*env)->GetStringUTFChars(env, id, NULL);
    const char *message = (*env)->GetStringUTFChars(env, result, NULL);
    webview_return(webview_data->webview, id_chars, -1, message);

    (*env)->ReleaseStringUTFChars(env, id, id_chars);
    (*env)->ReleaseStringUTFChars(env, id, message);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_setParentWindow(JNIEnv *env, jobject this, jobject awt_window) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    komelia_set_parent_window(env, webview_data->webview, awt_window);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_runMainLoop(JNIEnv *env, jobject this, jobject callback) {
    komelia_webview_t *webview_data = komelia_webview_from_jvm(env, this);
    webview_navigate(webview_data->webview, "about:blank");

    main_started_callback_t *started_callback = malloc(sizeof(main_started_callback_t));
    started_callback->jvm = webview_data->jvm;
    started_callback->object = (*env)->NewGlobalRef(env, callback);
    jclass runnable_class = (*env)->GetObjectClass(env, callback);
    started_callback->method = (*env)->GetMethodID(env, runnable_class, "run", "()V");

    webview_dispatch(webview_data->webview, komelia_main_started_callback, started_callback);
    webview_run(webview_data->webview);
}

JNIEXPORT void JNICALL Java_snd_webview_Webview_destroy(JNIEnv *env, jclass this, jlong ptr) {
    komelia_webview_t *webview_data = (komelia_webview_t *) ptr;
    g_hash_table_destroy(webview_data->bind_callbacks);
    g_hash_table_destroy(webview_data->resource_loaders);
    komelia_webview_destroy(webview_data->webview);
    free(webview_data);
}

JNIEXPORT jlong JNICALL
Java_snd_webview_Webview_create(JNIEnv *env, jobject this) {
    komelia_webview_t *webview_data = malloc(sizeof(komelia_webview_t));

    (*env)->GetJavaVM(env, &webview_data->jvm);
    webview_data->webview = komelia_webview_create();
    webview_data->bind_callbacks = g_hash_table_new_full(g_str_hash, g_str_equal,
                                                         g_free,
                                                         komelia_bind_callback_destroy);
    webview_data->resource_loaders = g_hash_table_new_full(g_str_hash, g_str_equal,
                                                           NULL,
                                                           komelia_resource_loader_destroy);

    return (jlong) webview_data;
}
