cmake_minimum_required(VERSION 3.25)
project(webview LANGUAGES C CXX)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib")
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib")

include(FetchContent)

FetchContent_Declare(
        webview
        GIT_REPOSITORY https://github.com/webview/webview
        GIT_TAG 0.12.0)
FetchContent_MakeAvailable(webview)

find_package(JNI REQUIRED COMPONENTS JVM)

add_library(
        komelia_webview SHARED
        src/gtk_webview.c
)
target_include_directories(komelia_webview PRIVATE ${JNI_INCLUDE_DIRS})
#target_sources(komelia_webview PRIVATE src/main.c)
target_link_libraries(komelia_webview PRIVATE webview::core_static)
install(TARGETS komelia_webview LIBRARY)
