include(ExternalProject)

ExternalProject_Add(ep_spng
        GIT_REPOSITORY https://github.com/randy408/libspng.git
        GIT_TAG v0.7.4
        DEPENDS ep_zlib
        CMAKE_ARGS
            -DCMAKE_BUILD_TYPE=Release
            -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/sysroot
            -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
            -DANDROID_ABI=${ANDROID_ABI}
            -DANDROID_PLATFORM=${ANDROID_PLATFORM}
            -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
            -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
            -DZLIB_ROOT:STRING=${CMAKE_BINARY_DIR}/sysroot
            -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)