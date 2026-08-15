include(ExternalProject)

ExternalProject_Add(ep_glib
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/glib
        DEPENDS ep_zlib ep_ffi ep_iconv
        PATCH_COMMAND git clean -dfx
        CONFIGURE_COMMAND ${Meson_EXECUTABLE} setup ${EP_MESON_ARGS} <BINARY_DIR> <SOURCE_DIR>
            -Dselinux=disabled
            -Dglib_debug=disabled
            -Dlibelf=disabled
            -Dintrospection=disabled
            -Dtests=false
        BUILD_COMMAND ${Meson_EXECUTABLE} compile -C <BINARY_DIR>
        INSTALL_COMMAND ${Meson_EXECUTABLE} install -C <BINARY_DIR>
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)