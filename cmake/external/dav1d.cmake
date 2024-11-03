include(ExternalProject)

ExternalProject_Add(ep_dav1d
        GIT_REPOSITORY https://code.videolan.org/videolan/dav1d.git
        GIT_TAG 1.5.0
        DEPENDS ep_zlib
        CONFIGURE_COMMAND ${Meson_EXECUTABLE} setup ${EP_MESON_ARGS} <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)