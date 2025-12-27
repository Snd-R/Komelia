#include "webview2_window.h"
#include <glib.h>
#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>
#include <shlwapi.h>
#include <unistd.h>
#include <WebView2.h>
#include <webview/webview.h>
#include <windows.h>
#include "../komelia_webview.h"
#include "../komelia_callbacks.h"
#include "win32_strings.h"

static int interceptor_is_registered = 0;
static request_interceptor *static_interceptor = NULL;
static GMutex mutex;

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    GHashTable *bind_callbacks;
    request_interceptor *interceptor;
    window_t webview_window;
} webview2_data;

HWND get_awt_hwnd(JNIEnv *env, jobject awt_window) {
    HWND awt_hwnd = NULL;
    JAWT_DrawingSurface *ds;
    JAWT_DrawingSurfaceInfo *dsi;
    jint lock;
    JAWT awt;
    awt.version = JAWT_VERSION_9;

    if (!JAWT_GetAWT(env, &awt)) {
        komelia_throw_jvm_exception(env, "Can't load JAWT");
        return NULL;
    }

    ds = awt.GetDrawingSurface(env, awt_window);
    if (ds == NULL) {
        komelia_throw_jvm_exception(env, "Can't get drawing surface");
        return NULL;
    }

    lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        komelia_throw_jvm_exception(env, "Can't get drawing surface lock");
        return NULL;
    }

    dsi = ds->GetDrawingSurfaceInfo(ds);
    if (dsi == NULL) {
        komelia_throw_jvm_exception(env, "Can't get drawing surface info");
    } else {
        JAWT_Win32DrawingSurfaceInfo *wdsi = dsi->platformInfo;
        if (wdsi != NULL) {
            awt_hwnd = wdsi->hwnd;
        } else {
            komelia_throw_jvm_exception(env, "Can't get w32 platform info");
        }
        ds->FreeDrawingSurfaceInfo(dsi);
    }
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
    return awt_hwnd;
}

webview_t komelia_webview_create(JNIEnv *env, jobject awt_window) {
    webview2_data *webview_data = malloc(sizeof(webview2_data));
    (*env)->GetJavaVM(env, &webview_data->jvm);
    webview_data->bind_callbacks = g_hash_table_new_full(g_str_hash,
                                                         g_str_equal,
                                                         NULL,
                                                         komelia_bind_callback_destroy);
    webview_data->interceptor = NULL;

    window_t webview_window = create_webview_window();
    webview_data->webview = webview_window->webview;
    webview_data->webview_window = webview_window;

    ICoreWebView2Controller *webview_controller = webview_get_native_handle(webview_window->webview,
                                                                            WEBVIEW_NATIVE_HANDLE_KIND_BROWSER_CONTROLLER);

    HWND hwnd = get_awt_hwnd(env, awt_window);
    HWND controller_parent = NULL;
    webview_controller->lpVtbl->get_ParentWindow(webview_controller, &controller_parent);
    SetParent(controller_parent, hwnd);
    return webview_data;
}

void komelia_webview_destroy_callback(webview_t webview, void *data) {
    g_mutex_lock(&mutex);
    webview2_data *webview_data = data;

    webview_terminate(webview);
    webview_destroy(webview);
    DestroyWindow(webview_data->webview_window->native_window);

    free(webview_data->webview_window);
    free(webview_data);
    interceptor_is_registered = 0;

    g_mutex_unlock(&mutex);
}

void komelia_webview_destroy(komelia_webview_t data) {
    webview2_data *webview_data = data;

    if (webview_data->interceptor != NULL) { komelia_interceptor_destroy(webview_data->interceptor); }
    g_hash_table_destroy(webview_data->bind_callbacks);

    SetParent(webview_data->webview_window->native_window, NULL);
    webview_dispatch(webview_data->webview, komelia_webview_destroy_callback, webview_data);
}

webview_t komelia_webview_get_webview(komelia_webview_t data) {
    return ((webview2_data *) data)->webview;
}

JavaVM *komelia_webview_get_jvm(komelia_webview_t data) {
    return ((webview2_data *) data)->jvm;
}

void komelia_webview_bind_cb(webview_t webview, void *data) {
    bind_callback_t *callback = data;
    webview_bind(webview, callback->name_chars, komelia_bind_callback_run, callback);
}

void komelia_webview_bind(komelia_webview_t webview, bind_callback_t *callback) {
    webview2_data *webview_data = webview;
    g_hash_table_insert(webview_data->bind_callbacks, callback->name_chars, callback);
    webview_dispatch(webview_data->webview, komelia_webview_bind_cb, callback);
}

//=====RESOURCE LOADING=====
ULONG HandlerAddRef(ICoreWebView2WebResourceRequestedEventHandler *This) {
    return 1;
}

ULONG HandlerRelease(ICoreWebView2WebResourceRequestedEventHandler *This) {
    return 1;
}

HRESULT HandlerQueryInterface(
        ICoreWebView2WebResourceRequestedEventHandler *This,
        const IID *riid,
        void **ppvObject
) {
    *ppvObject = This;
    return S_OK;
}

typedef struct {
    ICoreWebView2_2 *webview2;
    ICoreWebView2Environment *environment;
    IStream *memory_stream;
    ICoreWebView2WebResourceRequest *request;
    ICoreWebView2WebResourceResponse *response;
    load_result_t *result;
    boolean result_data_taken;
} interceptor_resources;

void free_interceptor_resources(interceptor_resources resources) {
    if (resources.webview2) resources.webview2->lpVtbl->Release(resources.webview2);
    if (resources.environment) resources.environment->lpVtbl->Release(resources.environment);
    if (resources.memory_stream) resources.memory_stream->lpVtbl->Release(resources.memory_stream);
    if (resources.response) resources.response->lpVtbl->Release(resources.response);
    if (resources.request) resources.request->lpVtbl->Release(resources.request);
    if (resources.result) {
        free(resources.result->data);
        free(resources.result);
    }
}

HRESULT RequestHandlerInvoke(
        ICoreWebView2WebResourceRequestedEventHandler *This,
        ICoreWebView2 *sender,
        ICoreWebView2WebResourceRequestedEventArgs *args
) {
    if (static_interceptor == NULL) {
        return E_FAIL;
    }

    interceptor_resources resources = {0};
    if (args->lpVtbl->get_Request(args, &resources.request)) {
        return E_FAIL;
    }

    LPWSTR uri_wide = NULL;
    resources.request->lpVtbl->get_Uri(resources.request, &uri_wide);
    char *uri_utf8 = toUTF8(uri_wide, 0, NULL);

    if (sender->lpVtbl->QueryInterface(sender, &IID_ICoreWebView2_2, (void **) &resources.webview2) != S_OK) {
        free_interceptor_resources(resources);
        return E_FAIL;
    }
    if (resources.webview2->lpVtbl->get_Environment(resources.webview2, &resources.environment) != S_OK) {
        free_interceptor_resources(resources);
        return E_FAIL;
    }
    resources.result = komelia_interceptor_run(static_interceptor, uri_utf8);
    if (resources.result == NULL) {
        free_interceptor_resources(resources);
        return E_FAIL;
    }
    resources.memory_stream = SHCreateMemStream(resources.result->data, resources.result->size);
    if (resources.memory_stream == NULL) {
        free_interceptor_resources(resources);
        return E_FAIL;
    }
    resources.result_data_taken = 1;

    LPWSTR versionInfo = NULL;
    resources.environment->lpVtbl->get_BrowserVersionString(resources.environment, &versionInfo);

    char *content_type = resources.result->content_type;
    wchar_t *response_header = NULL;
    if (content_type != NULL) {
        char *content_type_header = "Content-Type: ";
        char *tmp_content_type = malloc(sizeof(char) * (16 + strlen(content_type)));
        strcpy(tmp_content_type, content_type_header);
        strcat(tmp_content_type, content_type);

        //TODO response header is leaked. Only passed on initial index.html load. (Content-Type: text/html) ~48 bytes
        // handle release in ICoreWebView2WebResourceResponseReceivedEventHandler ???
        response_header = fromUTF8(tmp_content_type, 0, NULL);
        free(tmp_content_type);
    }
    if (resources.environment->lpVtbl->CreateWebResourceResponse(
            resources.environment,
            resources.memory_stream,
            200,
            L"OK",
            response_header,
            &resources.response
    ) != S_OK
            ) {
        free_interceptor_resources(resources);
        return E_FAIL;
    }

    args->lpVtbl->put_Response(args, resources.response);
    free_interceptor_resources(resources);
    return S_OK;
}

static ICoreWebView2WebResourceRequestedEventHandlerVtbl resource_requested_vtbl = {
        HandlerQueryInterface,
        HandlerAddRef,
        HandlerRelease,
        RequestHandlerInvoke
};
static ICoreWebView2WebResourceRequestedEventHandler resource_requested_handler = {&resource_requested_vtbl};

void komelia_register_interceptor_cb(webview_t webview, void *) {
    ICoreWebView2 *webview2 = NULL;
    ICoreWebView2Controller *controller = webview_get_native_handle(webview,
                                                                    WEBVIEW_NATIVE_HANDLE_KIND_BROWSER_CONTROLLER);
    controller->lpVtbl->get_CoreWebView2(controller, &webview2);
    if (webview2 == NULL) {
        fprintf(stderr, "Failed to register resource request filter\n");
        return;
    }

    webview2->lpVtbl->AddWebResourceRequestedFilter(webview2, L"*", COREWEBVIEW2_WEB_RESOURCE_CONTEXT_ALL);
    webview2->lpVtbl->add_WebResourceRequested(webview2, &resource_requested_handler, NULL);
    webview2->lpVtbl->Release(webview2);
}

void komelia_register_request_interceptor(komelia_webview_t webview, request_interceptor *interceptor) {
    g_mutex_lock(&mutex);
    webview2_data *webview_data = webview;
    if (webview_data->interceptor != NULL) {
        komelia_interceptor_destroy(webview_data->interceptor);
    }
    webview_data->interceptor = interceptor;
    static_interceptor = interceptor;

    if (interceptor_is_registered) {
        g_mutex_unlock(&mutex);
        return;
    }

    webview_dispatch(webview_data->webview, komelia_register_interceptor_cb, NULL);

    interceptor_is_registered = 1;
    g_mutex_unlock(&mutex);
}
