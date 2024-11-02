cmake_minimum_required(VERSION 3.25)


find_program(Make_EXECUTABLE make)
if(NOT Make_EXECUTABLE)
    message(FATAL_ERROR "Make is required")
endif()

find_program(Meson_EXECUTABLE meson)
if(NOT Meson_EXECUTABLE)
    message(FATAL_ERROR "Meson is required")
endif()

find_program(Ninja_EXECUTABLE ninja)
if(NOT Ninja_EXECUTABLE)
    message(FATAL_ERROR "Ninja is required")
endif()

project(komelia_superbuild C CXX)

set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/sysroot)
set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH}/lib;${CMAKE_FIND_ROOT_PATH})
set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})

include(ExternalProject)

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
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/sysroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -DCMAKE_SYSROOT=${CMAKE_SYSROOT}
        -DONNXRUNTIME_PATH=${THIRD_PARTY_LIB_PATH}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
        -DVULKAN_GPU_ENUMERATION=${VULKAN_GPU_ENUMERATION}
        -DCUDA_GPU_ENUMERATION=${CUDA_GPU_ENUMERATION}
        -DROCM_GPU_ENUMERATION=${ROCM_GPU_ENUMERATION}
        -DDXGI_GPU_ENUMERATION=${DXGI_GPU_ENUMERATION}
        -DCUDA_CUSTOM_PATH=${CUDA_CUSTOM_PATH}
        -DSKIA_CUSTOM_PATH=${SKIA_CUSTOM_PATH}
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)


ExternalProject_Add(ep_komelia_webview
    DOWNLOAD_COMMAND ""
    UPDATE_COMMAND ""
    DEPENDS ep_glib
    SOURCE_DIR ${PROJECT_SOURCE_DIR}/../komelia-webview/native
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/sysroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -DCMAKE_SYSROOT=${CMAKE_SYSROOT}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
        -DWEBVIEW_USE_COMPAT_MINGW=ON
        -DWEBVIEW_MSWEBVIEW2_VERSION=1.0.2592.51
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)
