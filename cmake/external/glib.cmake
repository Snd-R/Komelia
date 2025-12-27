include(ExternalProject)

ExternalProject_Add(ep_glib
        URL https://download.gnome.org/sources/glib/2.86/glib-2.86.1.tar.xz
        URL_HASH SHA256=119d1708ca022556d6d2989ee90ad1b82bd9c0d1667e066944a6d0020e2d5e57
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