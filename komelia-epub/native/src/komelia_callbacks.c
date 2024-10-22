#include "komelia_callbacks.h"

#include <glib.h>
#include <stdbool.h>

bool jvm_atach_if_necessary(JavaVM *jvm, JNIEnv **env) {
    (*jvm)->GetEnv(jvm, (void **) env,JNI_VERSION_10);
    if (*env == NULL) {
        JavaVMAttachArgs args;
        args.version = JNI_VERSION_10;
        (*jvm)->AttachCurrentThread(jvm, (void **) env, &args);
        return 1;
    }
    return 0;
}

void komelia_main_started_callback(webview_t, void *data) {
    main_started_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    bool requires_detach = jvm_atach_if_necessary(jvm, &env);

    (*env)->CallObjectMethod(env, callback->object, callback->method);
    (*env)->DeleteGlobalRef(env, callback->object);
    free(data);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

void komelia_bind_callback(const char *id, const char *req, void *data) {
    bind_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_atach_if_necessary(jvm, &env);

    jstring jvm_id = (*env)->NewStringUTF(env, id);
    jstring jvm_req = (*env)->NewStringUTF(env, req);
    (*env)->CallObjectMethod(env, callback->object, callback->method, jvm_id, jvm_req);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

void komelia_bind_callback_destroy(void *data) {
    bind_callback_t *callback = data;
    JavaVM *jvm = callback->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_atach_if_necessary(jvm, &env);

    const char *jvm_name_chars = (*env)->GetStringUTFChars(env, callback->name, 0);
    webview_unbind(callback->webview, jvm_name_chars);
    (*env)->ReleaseStringUTFChars(env, callback->name, jvm_name_chars);

    (*env)->DeleteGlobalRef(env, callback->name);
    (*env)->DeleteGlobalRef(env, callback->object);
    free(callback);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}


load_result_t *komelia_load_resource(resource_loader_t *loader, const char *uri) {
    JavaVM *jvm = loader->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_atach_if_necessary(jvm, &env);

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

    result->data = result_bytes;
    result->size = bytes_size;

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }

    return result;
}

void komelia_resource_loader_destroy(void *data) {
    resource_loader_t *loader = data;
    JavaVM *jvm = loader->jvm;
    JNIEnv *env = NULL;
    int requires_detach = jvm_atach_if_necessary(jvm, &env);

    (*env)->DeleteGlobalRef(env, loader->object);
    free(loader->scheme);
    free(loader);

    if (requires_detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}
