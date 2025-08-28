cmake_minimum_required(VERSION 3.25)
project(komelia_onnxruntime C CXX)
set(CMAKE_C_STANDARD 23)
include(CTest)
include(CMakePrintHelpers)

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
find_package(OpenMP REQUIRED)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")

pkg_search_module(GLIB2 REQUIRED glib-2.0 IMPORTED_TARGET)

if (NOT DEFINED ONNXRUNTIME_PATH)
    message(FATAL_ERROR "ONNXRUNTIME_PATH is not specified")
endif ()
find_library(ONNXRUNTIME_LIB NAMES onnxruntime PATHS ${ONNXRUNTIME_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
if (NOT DEFINED ONNXRUNTIME_LIB)
    message(FATAL_ERROR "Failed to find onnxruntime library")
endif ()
cmake_print_variables(ONNXRUNTIME_LIB)

find_library(KOMELIA_VIPS_LIB NAMES komelia_vips PATH_SUFFIXES lib)
include_directories(${CMAKE_SOURCE_DIR}/komelia-image-decoder/vips/native/src/vips/)
add_library(komelia_onnxruntime SHARED
        src/onnxruntime/jni/komelia_onnxruntime_common_jni.h
        src/onnxruntime/jni/komelia_onnxruntime_jni.c
        src/onnxruntime/jni/komelia_upscaler_jni.c
        src/onnxruntime/jni/komelia_rf_detr_jni.c
        src/onnxruntime/komelia_matrix_ops.h
        src/onnxruntime/win32_strings.h
        src/onnxruntime/komelia_ort_upscaler.h
        src/onnxruntime/komelia_ort_upscaler.c
        src/onnxruntime/komelia_onnxruntime.h
        src/onnxruntime/yolo/komelia_onnxruntime_yolo_detect.h
        src/onnxruntime/yolo/komelia_onnxruntime_yolo_detect.c
        src/onnxruntime/komelia_onnxruntime.c
        src/onnxruntime/komelia_error.h
        src/onnxruntime/komelia_error.c
        src/onnxruntime/komelia_ort_rf_detr.h
        src/onnxruntime/komelia_ort_rf_detr.c
)

if (${KOMELIA_VIPS_LIB} STREQUAL "KOMELIA_VIPS_LIB-NOTFOUND")
    set(KOMELIA_VIPS_LIBS komelia_vips)
else ()
    set(KOMELIA_VIPS_LIBS ${KOMELIA_VIPS_LIB})
endif ()
target_include_directories(komelia_onnxruntime PRIVATE
        ${VIPS_INCLUDE_DIRS}
        ${JNI_INCLUDE_DIRS}
        ${GLIB2_INCLUDE_DIRS}
        ${ONNXRUNTIME_PATH}/include
        ${KOMELIA_VIPS_INCLUDE}
)
target_link_libraries(komelia_onnxruntime
        m
        PkgConfig::VIPS
        Threads::Threads
        OpenMP::OpenMP_C
        PkgConfig::GLIB2
        ${KOMELIA_VIPS_LIBS}
        ${ONNXRUNTIME_LIB}
)
install(TARGETS komelia_onnxruntime LIBRARY)

if (CUDA_GPU_ENUMERATION)
    find_package(CUDAToolkit)
    if (CUDAToolkit_FOUND OR CUDA_CUSTOM_PATH)
        add_library(komelia_enumerate_devices_cuda SHARED
                src/onnxruntime/device/komelia_enumerate_devices.h
                src/onnxruntime/device/komelia_enumerate_devices_cuda.c
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
            src/onnxruntime/win32_strings.h
    )
    target_include_directories(komelia_enumerate_devices_dxgi PRIVATE
            ${JNI_INCLUDE_DIRS}
    )
    target_link_libraries(komelia_enumerate_devices_dxgi dxgi)
    install(TARGETS komelia_enumerate_devices_dxgi LIBRARY)
endif ()

if (WIN32)
    add_library(komelia_onnxruntime_dml SHARED
            src/vips/vips_common_jni.h
            src/vips/vips_common_jni.c
            src/onnxruntime/jni/komelia_onnxruntime_jni.c
            src/onnxruntime/komelia_matrix_ops.h
            src/onnxruntime/onnxruntime_conversions.c
            src/onnxruntime/win32_strings.h
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

#enable_testing()
#
#add_executable(upscaler1_test test/upscaler_test.c)
#target_link_libraries(upscaler1_test komelia_onnxruntime)
#target_include_directories(upscaler1_test PRIVATE ${ONNXRUNTIME_PATH}/include)
#add_test(upscaler_test upscaler1_test)
#
#add_executable(rfdetr_test1 test/rfdetr_test.c)
#target_link_libraries(rfdetr_test1 komelia_onnxruntime)
#target_include_directories(rfdetr_test1 PRIVATE ${ONNXRUNTIME_PATH}/include)
#add_test(rfdetr_test rfdetr_test1)
