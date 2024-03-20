include(ExternalProject)

list(APPEND DEPENDENCIES ep_dav1d)
ExternalProject_Add(ep_dav1d
        GIT_REPOSITORY https://code.videolan.org/videolan/dav1d.git
        GIT_TAG 1.4.1
        CONFIGURE_COMMAND
        PKG_CONFIG_PATH=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib LD_LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib:$ENV{LD_LIBRARY_PATH}
         ${Meson_EXECUTABLE} setup ${MESON_CROSS_FILE_ARG} --prefix=<INSTALL_DIR> --libdir=lib <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND
        ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND
        ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        DEPENDS
        ep_zlib
)