include(ExternalProject)

ExternalProject_Add(ep_zlib
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/zlib-ng
        UPDATE_DISCONNECTED True
        PATCH_COMMAND git reset --hard && git clean -dfx && patch < ${CMAKE_CURRENT_LIST_DIR}/patches/zlib-ng-2-fixes.patch
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DINSTALL_PKGCONFIG_DIR=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig
            -DZLIB_COMPAT=ON
            -DZLIB_ENABLE_TESTS=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)