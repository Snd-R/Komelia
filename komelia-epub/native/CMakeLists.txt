cmake_minimum_required(VERSION 3.25)
project(webview LANGUAGES C CXX)
include(FetchContent)

FetchContent_Declare(
        webview
        GIT_REPOSITORY https://github.com/webview/webview
        GIT_TAG 0.12.0)
FetchContent_MakeAvailable(webview)

find_package(JNI REQUIRED COMPONENTS JVM)

add_library(
        komelia_webview SHARED
        src/komelia_webview.h
        src/komelia_webview_webkit2gtk.c
        src/webview_jni.c
        src/komelia_callbacks.h
        src/komelia_callbacks.c
)
target_include_directories(komelia_webview PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(komelia_webview PRIVATE webview::core_static)
