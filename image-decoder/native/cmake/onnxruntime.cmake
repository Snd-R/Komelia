include(ExternalProject)

ExternalProject_Add(ep_onnxruntime
    GIT_REPOSITORY https://github.com/Microsoft/onnxruntime.git
    GIT_TAG v1.18.0
    PATCH_COMMAND patch -p 1 < ${CMAKE_CURRENT_LIST_DIR}/patches/onnxruntime_cmake.patch
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
        -DBUILD_SHARED_LIBS=ON
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -Donnxruntime_BUILD_SHARED_LIB=ON
        -Donnxruntime_BUILD_UNIT_TESTS=OFF
        -DBUILD_TESTING=OFF
    SOURCE_SUBDIR cmake
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)
