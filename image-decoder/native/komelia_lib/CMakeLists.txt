cmake_minimum_required(VERSION 3.25)
project(komelia C)
set(CMAKE_C_STANDARD 23)

OPTION(VULKAN_GPU_ENUMERATION "build gpu enumeration shared lib for vulkan" ON)
OPTION(CUDA_GPU_ENUMERATION "build gpu enumeration shared lib for cuda" OFF)
OPTION(ROCM_GPU_ENUMERATION "build gpu enumeration shared lib for rocm" OFF)
OPTION(DXGI_GPU_ENUMERATION "build gpu enumeration shared lib for dxgi" OFF)

if (ANDROID)
    find_package(JNI REQUIRED)
else ()
    find_package(JNI REQUIRED COMPONENTS JVM)
endif ()

find_package(OpenMP REQUIRED)
find_package(PkgConfig REQUIRED)
pkg_check_modules(VIPS REQUIRED IMPORTED_TARGET vips)
find_package(Threads REQUIRED)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")

add_library(komelia_vips SHARED
        src/vips/vips_common_jni.h
        src/vips/vips_common_jni.c
        src/vips/komelia_vips.c
)
target_include_directories(komelia_vips PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS})
target_link_libraries(komelia_vips PkgConfig::VIPS)
install(TARGETS komelia_vips LIBRARY)

if (NOT ANDROID)
    find_library(ONNXRUNTIME_LIB NAMES onnxruntime PATHS ${ONNXRUNTIME_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)

    add_library(komelia_vips_ort SHARED
            src/vips/vips_common_jni.h
            src/vips/vips_common_jni.c
            src/onnxruntime/komelia_onnxruntime.c
            src/onnxruntime/onnxruntime_conversions.h
            src/onnxruntime/onnxruntime_conversions.c
            src/onnxruntime/win32_strings.h
            src/onnxruntime/win32_strings.c
    )
    target_include_directories(komelia_vips_ort PRIVATE
            ${VIPS_INCLUDE_DIRS}
            ${JNI_INCLUDE_DIRS}
            ${ONNXRUNTIME_PATH}/include
            ${ONNXRUNTIME_PATH}/include/onnxruntime
    )
    target_link_libraries(komelia_vips_ort
            m
            PkgConfig::VIPS
            Threads::Threads
            OpenMP::OpenMP_C
            ${ONNXRUNTIME_LIB}
    )
    install(TARGETS komelia_vips_ort LIBRARY)

    if (CUDA_GPU_ENUMERATION)
        add_library(komelia_enumerate_devices_cuda SHARED
                src/onnxruntime/device/komelia_enumerate_devices.h
                src/onnxruntime/device/komelia_enumerate_devices_cuda.c
                src/onnxruntime/device/devices_common_jni.h
                src/onnxruntime/device/devices_common_jni.c
        )

        find_package(CUDAToolkit)
        message(STATUS, "${CUDA_CUSTOM_PATH}")
        if (CUDAToolkit_FOUND)
            target_include_directories(komelia_enumerate_devices_cuda PRIVATE
                    ${JNI_INCLUDE_DIRS}
                    ${CUDAToolkit_INCLUDE_DIRS}
            )
            target_link_libraries(komelia_enumerate_devices_cuda CUDA::cudart)
        elseif (CUDA_CUSTOM_PATH)
            target_include_directories(komelia_enumerate_devices_cuda PRIVATE
                    ${JNI_INCLUDE_DIRS}
                    "${CUDA_CUSTOM_PATH}/include"
            )
            find_library(CUDART_LIB NAMES cudart PATHS ${CUDA_CUSTOM_PATH}/lib PATH_SUFFIXES x64 REQUIRED NO_DEFAULT_PATH)
            target_link_libraries(komelia_enumerate_devices_cuda ${CUDART_LIB})
        else ()
            message(FATAL_ERROR "Can't find cudart library")
        endif ()
        install(TARGETS komelia_enumerate_devices_cuda LIBRARY)
    endif ()

    if (VULKAN_GPU_ENUMERATION)
        find_package(Vulkan REQUIRED)
        add_library(komelia_enumerate_devices_vulkan SHARED
                src/onnxruntime/device/komelia_enumerate_devices.h
                src/onnxruntime/device/komelia_enumerate_devices_vulkan.c
                src/onnxruntime/device/devices_common_jni.h
                src/onnxruntime/device/devices_common_jni.c
        )
        target_include_directories(komelia_enumerate_devices_vulkan PRIVATE
                ${JNI_INCLUDE_DIRS}
                ${Vulkan_INCLUDE_DIRS}
        )
        target_link_libraries(komelia_enumerate_devices_vulkan Vulkan::Vulkan)
        install(TARGETS komelia_enumerate_devices_vulkan LIBRARY)
    endif ()

    if (DXGI_GPU_ENUMERATION AND WIN32)
        add_library(komelia_enumerate_devices_dxgi SHARED
                src/onnxruntime/device/komelia_enumerate_devices.h
                src/onnxruntime/device/komelia_enumerate_devices_dxgi.c
                src/onnxruntime/device/devices_common_jni.h
                src/onnxruntime/device/devices_common_jni.c
                src/onnxruntime/win32_strings.h
                src/onnxruntime/win32_strings.c
        )
        target_include_directories(komelia_enumerate_devices_dxgi PRIVATE
                ${JNI_INCLUDE_DIRS}
        )
        target_link_libraries(komelia_enumerate_devices_dxgi dxgi)
        install(TARGETS komelia_enumerate_devices_dxgi LIBRARY)
    endif ()

endif ()


if (WIN32)
    add_library(komelia_vips_ort_dml SHARED
            src/vips/vips_common_jni.h
            src/vips/vips_common_jni.c
            src/onnxruntime/komelia_onnxruntime.c
            src/onnxruntime/onnxruntime_conversions.h
            src/onnxruntime/onnxruntime_conversions.c
            src/onnxruntime/win32_strings.h
            src/onnxruntime/win32_strings.c
    )
    target_compile_definitions(komelia_vips_ort_dml PUBLIC USE_DML)
    target_include_directories(komelia_vips_ort_dml PRIVATE
            ${VIPS_INCLUDE_DIRS}
            ${JNI_INCLUDE_DIRS}
            ${ONNXRUNTIME_PATH}/include
            ${ONNXRUNTIME_PATH}/include/onnxruntime
    )
    target_link_libraries(komelia_vips_ort_dml
            m
            PkgConfig::VIPS
            Threads::Threads
            OpenMP::OpenMP_C
            ${ONNXRUNTIME_LIB}
    )
    install(TARGETS komelia_vips_ort_dml LIBRARY)
endif ()

if (ANDROID)
    add_library(komelia_android_bitmap SHARED
            src/vips/vips_common_jni.h
            src/vips/vips_common_jni.c
            src/android/komelia_android_bitmap.c
    )
    target_include_directories(komelia_android_bitmap PRIVATE
            ${VIPS_INCLUDE_DIRS}
            ${JNI_INCLUDE_DIRS}
    )
    target_link_libraries(komelia_android_bitmap
            PkgConfig::VIPS
            android
            jnigraphics log
    )
    install(TARGETS komelia_android_bitmap LIBRARY)
endif ()