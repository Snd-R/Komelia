include(ExternalProject)

ExternalProject_Add(ep_de265
    GIT_REPOSITORY      https://github.com/strukturag/libde265.git
    GIT_TAG             v1.0.15
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -DENABLE_SDL=OFF
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)