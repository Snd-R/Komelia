cmake_minimum_required(VERSION 3.25)

option(KOMELIA_SUPERBUILD "superbuild" ON)

if (KOMELIA_SUPERBUILD)
    project(komelia_superbuild C CXX)
    include(ExternalProject)

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


    set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/sysroot)
    set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH};${CMAKE_FIND_ROOT_PATH})
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

    include("cmake/external/zlib.cmake")
    include("cmake/external/brotli.cmake")
    include("cmake/external/ffi.cmake")
    include("cmake/external/iconv.cmake")
    include("cmake/external/expat.cmake")
    include("cmake/external/glib.cmake")
    include("cmake/external/highway.cmake")
    include("cmake/external/lcms2.cmake")
    include("cmake/external/exif.cmake")

    include("cmake/external/mozjpeg.cmake")
    include("cmake/external/jxl.cmake")
    include("cmake/external/spng.cmake")
    include("cmake/external/webp.cmake")
    include("cmake/external/dav1d.cmake")
    include("cmake/external/de265.cmake")
    include("cmake/external/heif.cmake")
    include("cmake/external/tiff.cmake")

    include("cmake/external/vips.cmake")
    include("cmake/external/onnxruntime.cmake")

    ExternalProject_Add(ep_komelia_vips
            DOWNLOAD_COMMAND ""
            UPDATE_COMMAND ""
            DEPENDS ep_vips
            SOURCE_DIR ${PROJECT_SOURCE_DIR}/komelia-image-decoder/vips/native
            CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            USES_TERMINAL_BUILD true
    )
    if (WIN32)
        set(KOMELIA_ONNXRUNTIME_DEPENDENCIES "ep_komelia_vips")
    else ()
        set(KOMELIA_ONNXRUNTIME_DEPENDENCIES ep_komelia_vips ep_onnxruntime)
    endif ()

    ExternalProject_Add(ep_komelia_onnxruntime
            DOWNLOAD_COMMAND ""
            UPDATE_COMMAND ""
            DEPENDS ${KOMELIA_ONNXRUNTIME_DEPENDENCIES}
            SOURCE_DIR ${PROJECT_SOURCE_DIR}/komelia-onnxruntime/native
            CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DCUDA_CUSTOM_PATH=${CUDA_CUSTOM_PATH}
            -DROCM_CUSTOM_PATH=${ROCM_CUSTOM_PATH}
            -DONNXRUNTIME_CUSTOM_PATH=${THIRD_PARTY_LIB_PATH}
            -DCUDA_GPU_ENUMERATION=${CUDA_GPU_ENUMERATION}
            -DROCM_GPU_ENUMERATION=${ROCM_GPU_ENUMERATION}
            -DDXGI_GPU_ENUMERATION=${DXGI_GPU_ENUMERATION}
            -DVULKAN_GPU_ENUMERATION=${VULKAN_GPU_ENUMERATION}
            USES_TERMINAL_BUILD true
    )

    if (NOT ANDROID)
        ExternalProject_Add(ep_komelia_webview
                DOWNLOAD_COMMAND ""
                UPDATE_COMMAND ""
                DEPENDS ep_glib
                SOURCE_DIR ${PROJECT_SOURCE_DIR}/komelia-webview/native
                CMAKE_ARGS
                ${EP_CMAKE_ARGS}
                -DWEBVIEW_USE_COMPAT_MINGW=${WEBVIEW_USE_COMPAT_MINGW}
                USES_TERMINAL_BUILD true
        )
    endif ()

else ()
    project(komelia C CXX)
    add_subdirectory(${PROJECT_SOURCE_DIR}/komelia-image-decoder/vips/native)

    if (NOT ANDROID)
        add_subdirectory(${PROJECT_SOURCE_DIR}/komelia-webview/native)
        add_subdirectory(${PROJECT_SOURCE_DIR}/komelia-onnxruntime/native)
    endif ()
endif ()