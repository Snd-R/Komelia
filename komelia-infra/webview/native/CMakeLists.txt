cmake_minimum_required(VERSION 3.25)
project(webview LANGUAGES C CXX)

find_package(PkgConfig REQUIRED)
pkg_search_module(GLIB2 REQUIRED glib-2.0 IMPORTED_TARGET)

include(FetchContent)
FetchContent_Declare(
        webview
        GIT_REPOSITORY https://github.com/webview/webview
        GIT_TAG 5c5db3f31bf644f673d9dc817e935815b590c30e
        UPDATE_DISCONNECTED True
        PATCH_COMMAND git apply ${CMAKE_CURRENT_SOURCE_DIR}/patches/use_base64_resolve.patch
)
FetchContent_MakeAvailable(webview)

if (WIN32)
    find_package(JNI REQUIRED)

    add_library(
            komelia_webview SHARED
            src/webview_jni.c
            src/komelia_callbacks.h
            src/komelia_callbacks.c
            src/komelia_webview.h
            src/webview2/komelia_webview_webview2.c
            src/webview2/win32_strings.h
            src/webview2/win32_strings.c
            src/webview2/webview2_window.c
            src/webview2/webview2_window.h
    )
    target_include_directories(komelia_webview PRIVATE
            ${JNI_INCLUDE_DIRS}
            ${GLIB_INCLUDE_DIRS}
    )
    target_link_libraries(komelia_webview PRIVATE
            webview::core_static
            ${JNI_LIBRARIES}
            PkgConfig::GLIB2
    )
    install(TARGETS komelia_webview LIBRARY)
else ()
    pkg_check_modules(WEBKIT_GTK IMPORTED_TARGET webkit2gtk-4.1)
    if (WEBKIT_GTK_FOUND)
        find_package(JNI REQUIRED)
        add_library(
                komelia_webview SHARED
                src/webview_jni.c
                src/komelia_callbacks.h
                src/komelia_callbacks.c
                src/komelia_webview.h
                src/webkit2gtk/komelia_webview_webkit2gtk.c
        )
        add_library(
                komelia_webkit_extension SHARED
                src/webkit2gtk/request_interceptor_extension.c
        )

        target_include_directories(komelia_webview PRIVATE ${JNI_INCLUDE_DIRS})
        target_link_libraries(komelia_webview PRIVATE webview::core_static ${JNI_LIBRARIES})
        target_link_libraries(komelia_webkit_extension PRIVATE PkgConfig::WEBKIT_GTK)

        install(TARGETS komelia_webview LIBRARY)
        install(TARGETS komelia_webkit_extension LIBRARY)
    endif ()
endif ()

