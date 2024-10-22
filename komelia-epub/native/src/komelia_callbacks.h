#ifndef KOMELIA_CALLBACKS_H
#define KOMELIA_CALLBACKS_H

#include <jni.h>
#include <webview/webview.h>

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    jstring name;
    jobject object;
    jmethodID method;
} bind_callback_t;

typedef struct {
    JavaVM *jvm;
    jobject object;
    jmethodID method;
} main_started_callback_t;

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    jobject object;
    jmethodID method;
    char *scheme;
} resource_loader_t;

typedef struct {
    void *data;
    long size;
    // const char *content_type;
} load_result_t;

void komelia_main_started_callback(webview_t webview, /*main_started_callback_t*/ void *data);

void komelia_bind_callback(const char *id, const char *req, /*bind_callback_t*/ void *data);

void komelia_bind_callback_destroy(/*bind_callback_t*/ void *data);

load_result_t *komelia_load_resource(resource_loader_t *loader, const char *uri);

void komelia_resource_loader_destroy(/*resource_loader_t*/ void *data);

#endif //KOMELIA_CALLBACKS_H
