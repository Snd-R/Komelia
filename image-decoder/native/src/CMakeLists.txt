cmake_minimum_required(VERSION 3.25)
project(komelia C)
set(CMAKE_C_STANDARD 23)

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

add_library(komelia_vips SHARED vips_common_jni.h vips_common_jni.c komelia_vips.c)
target_include_directories(komelia_vips PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS})
target_link_libraries(komelia_vips PkgConfig::VIPS)
install(TARGETS komelia_vips LIBRARY)

if (NOT ANDROID)
    find_library(ONNXRUNTIME_LIB NAMES onnxruntime PATHS ${ONNXRUNTIME_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
    add_library(komelia_vips_ort SHARED komelia_onnxruntime.c vips_common_jni.h vips_common_jni.c
            onnxruntime_conversions.h
            onnxruntime_conversions.c
    )
    target_include_directories(komelia_vips_ort PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS}
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
endif ()


if (WIN32)
    add_library(komelia_vips_ort_dml SHARED komelia_onnxruntime.c vips_common_jni.h vips_common_jni.c
            onnxruntime_conversions.h
            onnxruntime_conversions.c)
    target_compile_definitions(komelia_vips_ort_dml PUBLIC USE_DML)
    target_include_directories(komelia_vips_ort_dml PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS}
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
    add_library(komelia_android_bitmap SHARED komelia_android_bitmap.c vips_common_jni.h vips_common_jni.c)
    target_include_directories(komelia_android_bitmap PRIVATE
            ${VIPS_INCLUDE_DIRS}
            ${JNI_INCLUDE_DIRS}
    )
    target_link_libraries(komelia_android_bitmap PkgConfig::VIPS android jnigraphics log)
    install(TARGETS komelia_android_bitmap LIBRARY)
endif ()