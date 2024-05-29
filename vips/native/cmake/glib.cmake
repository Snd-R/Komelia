include(ExternalProject)

if ($ENV{TARGET} MATCHES ".*android.*")
    set(ENV_PATHS PKG_CONFIG_PATH=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib LD_LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib:$ENV{LD_LIBRARY_PATH})
    set(EXTRA_FLAGS -Dlibelf=disabled)
endif ()

list(APPEND DEPENDENCIES ep_glib)
ExternalProject_Add(ep_glib
        URL https://download.gnome.org/sources/glib/2.80/glib-2.80.2.tar.xz
        URL_HASH SHA256=b9cfb6f7a5bd5b31238fd5d56df226b2dda5ea37611475bf89f6a0f9400fe8bd
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