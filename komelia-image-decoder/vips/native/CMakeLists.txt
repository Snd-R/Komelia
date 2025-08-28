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
set_target_properties(komelia_vips
        PROPERTIES
        PUBLIC_HEADER src/vips/vips_common_jni.h
)
install(TARGETS komelia_vips LIBRARY PUBLIC_HEADER)

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