#include "komelia_callbacks.h"

#include <glib.h>
#include <stdbool.h>

bool jvm_attach_if_necessary(JavaVM *jvm, JNIEnv **env) {
    (*jvm)->GetEnv(jvm, (void **) env,JNI_VERSION_10);
    if (*env == NULL) {
        JavaVMAttachArgs args;
        args.version = JNI_VERSION_10;
        (*jvm)->AttachCurrentThread(jvm, (void **) env, &args);
        return 1;
    }
    return 0;
}

void komelia_main_started_callback(webview_t webview, void *data) {
    main_started_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    bool requires_detach = jvm_attach_if_necessary(jvm, &env);

    (*env)->CallObjectMethod(env, callback->object, callback->method);
    (*env)->DeleteGlobalRef(env, callback->object);
    free(data);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

void komelia_bind_callback_run(const char *id, const char *req, void *data) {
    bind_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_attach_if_necessary(jvm, &env);

    jstring jvm_id = (*env)->NewStringUTF(env, id);
    jstring jvm_req = (*env)->NewStringUTF(env, req);
    (*env)->CallObjectMethod(env, callback->object, callback->method, jvm_id, jvm_req);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}


load_result_t *komelia_interceptor_run(request_interceptor *loader, const char *uri) {
    JavaVM *jvm = loader->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_attach_if_necessary(jvm, &env);

    jstring jvm_req = (*env)->NewStringUTF(env, uri);
    jobject jvm_result = (*env)->CallObjectMethod(env, loader->object, loader->method, jvm_req);
    if (jvm_result == NULL) return NULL;

    jclass class = (*env)->GetObjectClass(env, jvm_result);

    jfieldID data_field = (*env)->GetFieldID(env, class, "data", "[B");
    jbyteArray jvm_byte_array = (*env)->GetObjectField(env, jvm_result, data_field);
    load_result_t *result = malloc(sizeof(load_result_t));

    jsize bytes_size = (*env)->GetArrayLength(env, jvm_byte_array);
    jbyte *jvm_bytes = (*env)->GetByteArrayElements(env, jvm_byte_array, NULL);
    unsigned char *result_bytes = malloc(bytes_size * sizeof(unsigned char));
    memcpy(result_bytes, jvm_bytes, bytes_size);

    jfieldID content_type_field = (*env)->GetFieldID(env, class, "contentType", "Ljava/lang/String;");
    jobject jvm_content_type = (*env)->GetObjectField(env, jvm_result, content_type_field);
    if (jvm_content_type != NULL) {
        const char *content_type_chars = (*env)->GetStringUTFChars(env, jvm_content_type, NULL);
        char *content_type = g_strdup(content_type_chars);
        (*env)->ReleaseStringUTFChars(env, jvm_content_type, content_type_chars);
        result->content_type = content_type;
    } else {
        result->content_type = NULL;
    }

    result->data = result_bytes;
    result->size = bytes_size;

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }

    return result;
}

request_interceptor *komelia_interceptor_create(JNIEnv *env, jobject jvm_interceptor, webview_t webview) {
    jclass class = (*env)->GetObjectClass(env, jvm_interceptor);
    jmethodID method = (*env)->GetMethodID(env, class, "run",
                                           "(Ljava/lang/String;)Lsnd/webview/ResourceLoadResult;");

    request_interceptor *interceptor = malloc(sizeof(request_interceptor));
    (*env)->GetJavaVM(env, &interceptor->jvm);
    interceptor->webview = webview;
    interceptor->object = (*env)->NewGlobalRef(env, jvm_interceptor);
    interceptor->method = method;

    return interceptor;
}

void komelia_interceptor_destroy(void *data) {
    request_interceptor *loader = data;
    JavaVM *jvm = loader->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_attach_if_necessary(jvm, &env);

    (*env)->DeleteGlobalRef(env, loader->object);
    free(loader);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

bind_callback_t *komelia_bind_callback_create(JNIEnv *env, jstring name, jobject jvm_callback, webview_t webview) {
    jclass class = (*env)->GetObjectClass(env, jvm_callback);
    jmethodID method = (*env)->GetMethodID(env, class, "run", "(Ljava/lang/String;Ljava/lang/String;)V");

    bind_callback_t *callback = malloc(sizeof(bind_callback_t));

    (*env)->GetJavaVM(env, &callback->jvm);
    callback->webview = webview;
    callback->name = (*env)->NewGlobalRef(env, name);
    callback->object = (*env)->NewGlobalRef(env, jvm_callback);
    callback->method = method;

    const char *jvm_name_chars = (*env)->GetStringUTFChars(env, name, 0);
    callback->name_chars = g_strdup(jvm_name_chars);;
    (*env)->ReleaseStringUTFChars(env, name, jvm_name_chars);

    return callback;

    // const char *jvm_name_chars = (*env)->GetStringUTFChars(env, name, 0);
    // callback->name_chars = g_strdup(jvm_name_chars);;
    // (*env)->ReleaseStringUTFChars(env, name, jvm_name_chars);
}

void komelia_bind_callback_destroy(void *data) {
    bind_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_attach_if_necessary(jvm, &env);

    const char *jvm_name_chars = (*env)->GetStringUTFChars(env, callback->name, 0);
    webview_unbind(callback->webview, jvm_name_chars);
    (*env)->ReleaseStringUTFChars(env, callback->name, jvm_name_chars);

    (*env)->DeleteGlobalRef(env, callback->name);
    (*env)->DeleteGlobalRef(env, callback->object);
    free(callback->name_chars);
    free(callback);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}
