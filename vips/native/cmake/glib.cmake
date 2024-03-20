include(ExternalProject)

if ($ENV{TARGET} MATCHES ".*android.*")
    set(ENV_PATHS PKG_CONFIG_PATH=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib LD_LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib:$ENV{LD_LIBRARY_PATH})
    set(EXTRA_FLAGS -Dlibelf=disabled)
endif ()

list(APPEND DEPENDENCIES ep_glib)
ExternalProject_Add(ep_glib
        #URL https://download.gnome.org/sources/glib/2.80/glib-2.80.0.tar.xz
        #URL_HASH SHA256=8228a92f92a412160b139ae68b6345bd28f24434a7b5af150ebe21ff587a561d
        URL https://download.gnome.org/sources/glib/2.78/glib-2.78.4.tar.xz
        URL_HASH SHA256=24b8e0672dca120cc32d394bccb85844e732e04fe75d18bb0573b2dbc7548f63
        CONFIGURE_COMMAND
        PKG_CONFIG_PATH=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig PKG_CONFIG_PATH_CUSTOM=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig LD_LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib:$ENV{LD_LIBRARY_PATH} ${ENV_PATHS}
        ${Meson_EXECUTABLE} setup -D selinux=disabled -D glib_debug=disabled ${MESON_CROSS_FILE_ARG} --prefix=<INSTALL_DIR> --libdir=lib
        <BINARY_DIR> <SOURCE_DIR>
        ${EXTRA_FLAGS}
        BUILD_COMMAND
        ${Meson_EXECUTABLE} compile -C <BINARY_DIR>
        INSTALL_COMMAND
        ${Meson_EXECUTABLE} install -C <BINARY_DIR>
        DEPENDS
        ep_zlib
        ep_ffi
)
list(APPEND EXTRA_CMAKE_ARGS -DGLIB_INCLUDE_1=${THIRD_PARTY_LIB_PATH}/include/glib-2.0 -DGLIB_INCLUDE_2=${THIRD_PARTY_LIB_PATH}/lib/glib-2.0/include)