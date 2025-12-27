#include "webview2_window.h"
#include <stddef.h>
#include <pthread.h>
#include "WebView2.h"

void resize_widget(window_t window) {
    if (window->webview) {
        HWND widget_handle = (HWND) webview_get_native_handle(
            window->webview, WEBVIEW_NATIVE_HANDLE_KIND_UI_WIDGET);
        if (widget_handle) {
            RECT r = {};
            if (GetClientRect(GetParent(widget_handle), &r)) {
                MoveWindow(widget_handle, r.left, r.top, r.right - r.left,
                           r.bottom - r.top, TRUE);
            }
        }
    }
}

void focus_webview(window_t window) {
    if (window->webview) {
        ICoreWebView2Controller *controller_ptr = webview_get_native_handle(window->webview,
                                                                            WEBVIEW_NATIVE_HANDLE_KIND_BROWSER_CONTROLLER);
        if (controller_ptr) {
            controller_ptr->lpVtbl->MoveFocus(controller_ptr,
                                              COREWEBVIEW2_MOVE_FOCUS_REASON_PROGRAMMATIC);
        }
    }
}

LRESULT wndproc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
    window_t self = NULL;

    if (msg == WM_NCCREATE) {
        LPCREATESTRUCT lpcs = (LPCREATESTRUCT) lp;
        self = (window_t) lpcs->lpCreateParams;
        self->native_window = hwnd;
        SetWindowLongPtrW(hwnd, GWLP_USERDATA, (LONG_PTR) self);
    } else {
        self = (window_t) GetWindowLongPtrW(hwnd, GWLP_USERDATA);
    }

    if (!self) {
        return DefWindowProcW(hwnd, msg, wp, lp);
    }

    switch (msg) {
        case WM_CREATE:
            self->webview = webview_create(1, self->native_window);
            break;
        case WM_SIZE:
            resize_widget(self);
            break;
        case WM_CLOSE:
            DestroyWindow(self->native_window);
            break;
        case WM_DESTROY:
            PostQuitMessage(0);
            break;
        case WM_ACTIVATE:
            if (LOWORD(wp) != WA_INACTIVE) {
                focus_webview(self);
            }
            break;
        default:
            return DefWindowProcW(self->native_window, msg, wp, lp);
    }
    return 0;
}

window_t create_webview_window() {
    CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
    HINSTANCE hInstance = GetModuleHandleW(NULL);

    WNDCLASSEXW wc = {};
    wc.cbSize = sizeof(WNDCLASSEX);
    wc.hInstance = hInstance;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH) (COLOR_WINDOW + 1);
    wc.lpszClassName = L"window";
    wc.lpfnWndProc = wndproc;
    RegisterClassExW(&wc);

    window_t window = (window_t) malloc(sizeof(struct window_));
    memset(window, 0, sizeof(struct window_));

    CreateWindowExW(0, L"window", L"komelia webview", WS_BORDER,
                    CW_USEDEFAULT, CW_USEDEFAULT, 400, 300, NULL, NULL, hInstance,
                    window);

    return window;
}
