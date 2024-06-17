include(ExternalProject)

if (MESON_CROSS_FILE)
    set(MESON_CROSS_FILE_ARG --cross-file=${MESON_CROSS_FILE})
endif()

ExternalProject_Add(ep_glib
        URL https://download.gnome.org/sources/glib/2.80/glib-2.80.2.tar.xz
        URL_HASH SHA256=b9cfb6f7a5bd5b31238fd5d56df226b2dda5ea37611475bf89f6a0f9400fe8bd
        DEPENDS ep_zlib ep_ffi ep_iconv
        CONFIGURE_COMMAND
            ${Meson_EXECUTABLE} setup  ${MESON_CROSS_FILE_ARG} --prefix=<INSTALL_DIR> --libdir=lib
            <BINARY_DIR> <SOURCE_DIR>
            -D selinux=disabled
            -D glib_debug=disabled
            -Dlibelf=disabled
            -Dintrospection=disabled
            -Dtests=false
        BUILD_COMMAND
            ${Meson_EXECUTABLE} compile -C <BINARY_DIR>
        INSTALL_COMMAND
            ${Meson_EXECUTABLE} install -C <BINARY_DIR>
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)