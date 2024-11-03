include(ExternalProject)

ExternalProject_Add(ep_glib
        URL https://download.gnome.org/sources/glib/2.82/glib-2.82.2.tar.xz
        URL_HASH SHA256=ab45f5a323048b1659ee0fbda5cecd94b099ab3e4b9abf26ae06aeb3e781fd63
        DEPENDS ep_zlib ep_ffi ep_iconv
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