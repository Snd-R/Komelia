include(ExternalProject)

ExternalProject_Add(ep_exif
        GIT_REPOSITORY https://github.com/libexif/libexif
        GIT_TAG v0.6.25
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        DEPENDS ep_zlib
        CONFIGURE_COMMAND
           cd <SOURCE_DIR> && autoreconf -i && ./configure ${HOST_FLAG} --prefix ${CMAKE_BINARY_DIR}/sysroot
        BUILD_COMMAND ${Make_EXECUTABLE} all
        INSTALL_COMMAND ${Make_EXECUTABLE} install
        BUILD_IN_SOURCE true
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)