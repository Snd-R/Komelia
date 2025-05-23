cmake_minimum_required(VERSION 3.25)
project(komelia_image_decoder C CXX)
set(CMAKE_C_STANDARD 23)

OPTION(CUDA_GPU_ENUMERATION "build gpu enumeration shared lib for cuda" OFF)
OPTION(DXGI_GPU_ENUMERATION "build gpu enumeration shared lib for dxgi" OFF)
OPTION(ROCM_GPU_ENUMERATION "build gpu enumeration shared lib for rocm" OFF)

if (ANDROID)
    find_package(JNI REQUIRED)
else ()
    find_package(JNI REQUIRED COMPONENTS JVM)
endif ()

find_package(PkgConfig REQUIRED)
pkg_check_modules(VIPS REQUIRED IMPORTED_TARGET vips)
find_package(Threads REQUIRED)

if (NOT ANDROID)
    find_package(OpenMP REQUIRED)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
endif ()

add_library(komelia_vips SHARED
        src/vips/vips_common_jni.h
        src/vips/vips_common_jni.c
        src/vips/komelia_vips.c
)
target_include_directories(komelia_vips PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS})
target_link_libraries(komelia_vips PkgConfig::VIPS)
install(TARGETS komelia_vips LIBRARY)

if (NOT ANDROID)
    find_library(ONNXRUNTIME_LIB NAMES onnxruntime PATHS ${ONNXRUNTIME_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    #pkg_check_modules(ONNXRUNTIME IMPORTED_TARGET libonnxruntime)
    #    if (ONNXRUNTIME_FOUND OR ONNXRUNTIME_LIB)
    if (ONNXRUNTIME_LIB)
        add_library(komelia_onnxruntime SHARED
                src/vips/vips_common_jni.h
                src/vips/vips_common_jni.c
                src/onnxruntime/komelia_onnxruntime.c
                src/onnxruntime/onnxruntime_conversions.h
                src/onnxruntime/onnxruntime_conversions.c
                src/onnxruntime/win32_strings.h
                src/onnxruntime/win32_strings.c
        )

        #        if (${ONNXRUNTIME_FOUND})
        #            target_include_directories(komelia_onnxruntime PRIVATE
        #                    ${VIPS_INCLUDE_DIRS}
        #                    ${JNI_INCLUDE_DIRS}
        #                    ${ONNXRUNTIME_INCLUDE_DIRS}
        #            )
        #            target_link_libraries(komelia_onnxruntime
        #                    m
        #                    PkgConfig::VIPS
        #                    Threads::Threads
        #                    OpenMP::OpenMP_C
        #                    ${ONNXRUNTIME_LIBRARIES}
        #            )
        #        else ()
        target_include_directories(komelia_onnxruntime PRIVATE
                ${VIPS_INCLUDE_DIRS}
                ${JNI_INCLUDE_DIRS}
                ${ONNXRUNTIME_PATH}/include
        )
        target_link_libraries(komelia_onnxruntime
                m
                PkgConfig::VIPS
                Threads::Threads
                OpenMP::OpenMP_C
                ${ONNXRUNTIME_LIB}
        )
        #        endif ()
        install(TARGETS komelia_onnxruntime LIBRARY)

        if (CUDA_GPU_ENUMERATION)
            find_package(CUDAToolkit)
            if (CUDAToolkit_FOUND OR CUDA_CUSTOM_PATH)
                add_library(komelia_enumerate_devices_cuda SHARED
                        src/onnxruntime/device/komelia_enumerate_devices.h
                        src/onnxruntime/device/komelia_enumerate_devices_cuda.c
                        src/onnxruntime/device/devices_common_jni.h
                        src/onnxruntime/device/devices_common_jni.c
                )

                if (CUDAToolkit_FOUND)
                    target_include_directories(komelia_enumerate_devices_cuda PRIVATE ${JNI_INCLUDE_DIRS} ${CUDAToolkit_INCLUDE_DIRS})
                    target_link_libraries(komelia_enumerate_devices_cuda CUDA::cudart)
                elseif (CUDA_CUSTOM_PATH)
                    target_include_directories(komelia_enumerate_devices_cuda PRIVATE ${JNI_INCLUDE_DIRS} "${CUDA_CUSTOM_PATH}/include")
                    find_library(CUDART_LIB NAMES cudart PATHS ${CUDA_CUSTOM_PATH}/lib PATH_SUFFIXES x64 REQUIRED NO_DEFAULT_PATH)
                    target_link_libraries(komelia_enumerate_devices_cuda ${CUDART_LIB})
                endif ()

                install(TARGETS komelia_enumerate_devices_cuda LIBRARY)
            else ()
                message(WARNING "Can't find cudart library, disabling cuda device enumeration support")
            endif ()
        endif ()

        if (ROCM_GPU_ENUMERATION)
            find_package(HIP)
            if (HIP_FOUND OR ROCM_CUSTOM_PATH)
                add_library(komelia_enumerate_devices_rocm SHARED
                        src/onnxruntime/device/komelia_enumerate_devices.h
                        src/onnxruntime/device/komelia_enumerate_devices_rocm.c
                        src/onnxruntime/device/devices_common_jni.h
                        src/onnxruntime/device/devices_common_jni.c
                )

                if (HIP_FOUND)
                    target_include_directories(komelia_enumerate_devices_rocm PRIVATE ${JNI_INCLUDE_DIRS} ${HIP_INCLUDE_DIRS})
                    target_link_libraries(komelia_enumerate_devices_rocm hip::host)
                elseif (ROCM_CUSTOM_PATH)
                    target_include_directories(komelia_enumerate_devices_rocm PRIVATE ${JNI_INCLUDE_DIRS} "${ROCM_CUSTOM_PATH}/include")
                    find_library(ROCM_HIP_LIB NAMES amdhip64 PATHS ${ROCM_CUSTOM_PATH}/lib REQUIRED NO_DEFAULT_PATH)
                    target_compile_definitions(komelia_enumerate_devices_rocm PUBLIC __HIP_PLATFORM_AMD__)
                    target_link_libraries(komelia_enumerate_devices_rocm ${ROCM_HIP_LIB})
                endif ()

                install(TARGETS komelia_enumerate_devices_rocm LIBRARY)
            else ()
                message(WARNING "Can't find cudart library, disabling rocm device enumeration support")
            endif ()

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
endif ()


if (WIN32)
    add_library(komelia_onnxruntime_dml SHARED
            src/vips/vips_common_jni.h
            src/vips/vips_common_jni.c
            src/onnxruntime/komelia_onnxruntime.c
            src/onnxruntime/onnxruntime_conversions.h
            src/onnxruntime/onnxruntime_conversions.c
            src/onnxruntime/win32_strings.h
            src/onnxruntime/win32_strings.c
    )
    target_compile_definitions(komelia_onnxruntime_dml PUBLIC USE_DML)
    target_include_directories(komelia_onnxruntime_dml PRIVATE
            ${VIPS_INCLUDE_DIRS}
            ${JNI_INCLUDE_DIRS}
            ${ONNXRUNTIME_PATH}/include
            ${ONNXRUNTIME_PATH}/include/onnxruntime
    )
    target_link_libraries(komelia_onnxruntime_dml
            m
            PkgConfig::VIPS
            Threads::Threads
            OpenMP::OpenMP_C
            ${ONNXRUNTIME_LIB}
    )
    install(TARGETS komelia_onnxruntime_dml LIBRARY)
endif ()

#if (SKIA_CUSTOM_PATH)
#    find_library(SKIA_LIB NAMES skia PATHS ${SKIA_CUSTOM_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
#    add_library(komelia_skia SHARED
#            src/skia/komelia_skia.cpp
#            src/vips/vips_common_jni.h
#    )
#    target_include_directories(komelia_skia PRIVATE
#            ${VIPS_INCLUDE_DIRS}
#            ${JNI_INCLUDE_DIRS}
#            ${SKIA_CUSTOM_PATH}
#    )
#    target_link_libraries(komelia_skia
#            komelia_vips
#            PkgConfig::VIPS
#            ${SKIA_LIB}
#    )
#    install(TARGETS komelia_skia LIBRARY)
#endif ()

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