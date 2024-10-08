include(ExternalProject)

ExternalProject_Add(ep_brotli
    GIT_REPOSITORY      https://github.com/google/brotli
    GIT_TAG             v1.1.0
    CMAKE_ARGS
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)