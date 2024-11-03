cmake_minimum_required(VERSION 3.25)

find_program(Make_EXECUTABLE make)
if (NOT Make_EXECUTABLE)
    message(FATAL_ERROR "Make is required")
endif ()

find_program(Meson_EXECUTABLE meson)
if (NOT Meson_EXECUTABLE)
    message(FATAL_ERROR "Meson is required")
endif ()

find_program(Ninja_EXECUTABLE ninja)
if (NOT Ninja_EXECUTABLE)
    message(FATAL_ERROR "Ninja is required")
endif ()

project(komelia_superbuild C CXX)
include(ExternalProject)

set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/sysroot)
set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH}/lib;${CMAKE_FIND_ROOT_PATH})
set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})

if (MESON_CROSS_FILE)
    set(MESON_CROSS_FILE_ARG --cross-file=${MESON_CROSS_FILE})
endif ()
set(EP_MESON_ARGS ${MESON_CROSS_FILE_ARG} --default-library shared --prefix=<INSTALL_DIR> --libdir=lib --buildtype=release)

set(EP_CMAKE_ARGS
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/sysroot
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_SYSROOT=${CMAKE_SYSROOT}
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -DCMAKE_FIND_LIBRARY_SUFFIXES=${CMAKE_FIND_LIBRARY_SUFFIXES}
        -DBUILD_SHARED_LIBS=ON
        -DBUILD_TESTING=OFF)

include("external/zlib.cmake")
include("external/brotli.cmake")
include("external/ffi.cmake")
include("external/iconv.cmake")
include("external/expat.cmake")
include("external/glib.cmake")
include("external/highway.cmake")

include("external/mozjpeg.cmake")
include("external/jxl.cmake")
include("external/spng.cmake")
include("external/webp.cmake")
include("external/dav1d.cmake")
include("external/de265.cmake")
include("external/heif.cmake")
include("external/tiff.cmake")

include("external/vips.cmake")

ExternalProject_Add(ep_komelia_vips
        DOWNLOAD_COMMAND ""
        UPDATE_COMMAND ""
        DEPENDS ep_vips
        SOURCE_DIR ${PROJECT_SOURCE_DIR}/../komelia-image-decoder/native
        CMAKE_ARGS
        ${EP_CMAKE_ARGS}
        -DCUDA_CUSTOM_PATH=${CUDA_CUSTOM_PATH}
        -DSKIA_CUSTOM_PATH=${SKIA_CUSTOM_PATH}
        -DONNXRUNTIME_PATH=${THIRD_PARTY_LIB_PATH}
        -DCUDA_GPU_ENUMERATION=${CUDA_GPU_ENUMERATION}
        -DROCM_GPU_ENUMERATION=${ROCM_GPU_ENUMERATION}
        -DDXGI_GPU_ENUMERATION=${DXGI_GPU_ENUMERATION}
        USES_TERMINAL_BUILD true
)

if (NOT ANDROID)
    ExternalProject_Add(ep_komelia_webview
            DOWNLOAD_COMMAND ""
            UPDATE_COMMAND ""
            DEPENDS ep_glib
            SOURCE_DIR ${PROJECT_SOURCE_DIR}/../komelia-webview/native
            CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWEBVIEW_USE_COMPAT_MINGW=${WEBVIEW_USE_COMPAT_MINGW}
            USES_TERMINAL_BUILD true
    )
endif ()
