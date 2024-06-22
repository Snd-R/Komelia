cmake_minimum_required(VERSION 3.25)
project(komelia C)
set(CMAKE_C_STANDARD 23)

find_package(JNI REQUIRED COMPONENTS JVM)
find_package(OpenMP REQUIRED)
find_package(PkgConfig REQUIRED)
pkg_check_modules(VIPS REQUIRED IMPORTED_TARGET vips)
find_library(ONNXRUNTIME_LIB NAMES onnxruntime PATHS ${ONNXRUNTIME_PATH} PATH_SUFFIXES lib REQUIRED NO_DEFAULT_PATH)
find_package(Threads REQUIRED)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")

add_library(komelia_vips SHARED komelia_vips_decoder.c vips_jni.h vips_jni.c)
add_library(komelia_vips_ort SHARED komelia_vips_ort_decoder.c vips_jni.h vips_jni.c
        ort_conversions.h
        ort_conversions.c)

target_include_directories(komelia_vips PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS})
target_include_directories(komelia_vips_ort PRIVATE ${VIPS_INCLUDE_DIRS} ${JNI_INCLUDE_DIRS}
        ${ONNXRUNTIME_PATH}/include
        ${ONNXRUNTIME_PATH}/include/onnxruntime
)

target_link_libraries(komelia_vips PkgConfig::VIPS)
target_link_libraries(komelia_vips_ort
        m
        PkgConfig::VIPS
        Threads::Threads
        OpenMP::OpenMP_C
        ${ONNXRUNTIME_LIB}
)

install(TARGETS komelia_vips LIBRARY)
install(TARGETS komelia_vips_ort LIBRARY)

if (WIN32)
    add_library(komelia_vips_ort_dml SHARED komelia_vips_ort_decoder.c vips_jni.h vips_jni.c
            ort_conversions.h
            ort_conversions.c)
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
