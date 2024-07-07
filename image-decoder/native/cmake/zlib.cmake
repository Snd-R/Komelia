include(ExternalProject)

ExternalProject_Add(ep_zlib
        GIT_REPOSITORY https://github.com/zlib-ng/zlib-ng.git
        GIT_TAG 2.1.6
        UPDATE_DISCONNECTED True
        CMAKE_ARGS
            -DCMAKE_BUILD_TYPE=Release
            -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
            -DBUILD_SHARED_LIBS=ON
            -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
            -DANDROID_ABI=${ANDROID_ABI}
            -DANDROID_PLATFORM=${ANDROID_PLATFORM}
            -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
            -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
            -DINSTALL_PKGCONFIG_DIR=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig
            -DZLIB_COMPAT=ON
            -DZLIB_ENABLE_TESTS=OFF
         PATCH_COMMAND patch < ${CMAKE_CURRENT_LIST_DIR}/patches/zlib-ng-2-fixes.patch
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)