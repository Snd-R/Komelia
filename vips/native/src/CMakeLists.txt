cmake_minimum_required(VERSION 3.18)
project(komelia C)

set(CMAKE_C_STANDARD 11)

add_library(komelia SHARED vips_wrapper.c)

include(CMakeFindDependencyMacro)
find_library(VIPS_LIB NAMES vips PATHS ${CUSTOM_LIB_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
find_library(GLIB_LIB NAMES glib-2.0 PATHS ${CUSTOM_LIB_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
find_library(GOBJECT_LIB NAMES gobject-2.0 PATHS ${CUSTOM_LIB_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
find_package(JNI REQUIRED COMPONENTS JVM)

target_link_libraries(komelia
        ${VIPS_LIB}
        ${GLIB_LIB}
        ${GOBJECT_LIB}
)

target_include_directories(komelia PRIVATE
        ${CUSTOM_LIB_PATH}/include
        ${CUSTOM_LIB_PATH}/include/glib-2.0
        ${CUSTOM_LIB_PATH}/lib/glib-2.0/include
        ${JNI_INCLUDE_DIRS}
)

install(TARGETS komelia LIBRARY)
