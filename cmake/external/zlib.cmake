include(ExternalProject)

ExternalProject_Add(ep_zlib
        GIT_REPOSITORY https://github.com/zlib-ng/zlib-ng.git
        GIT_TAG 2.3.2
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        UPDATE_DISCONNECTED True
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DINSTALL_PKGCONFIG_DIR=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig
            -DZLIB_COMPAT=ON
            -DZLIB_ENABLE_TESTS=OFF
        PATCH_COMMAND patch < ${CMAKE_CURRENT_LIST_DIR}/patches/zlib-ng-2-fixes.patch
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)