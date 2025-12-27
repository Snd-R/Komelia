#ifndef WEBVIEW2_WINDOW_H
#define WEBVIEW2_WINDOW_H

#include "webview/webview.h"
#include <windows.h>

typedef struct window_ {
    HWND native_window;
    webview_t webview;
} *window_t;


window_t create_webview_window();

#endif //WEBVIEW2_WINDOW_H
