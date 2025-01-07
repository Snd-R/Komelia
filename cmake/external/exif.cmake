include(ExternalProject)

ExternalProject_Add(ep_exif
        GIT_REPOSITORY https://github.com/libexif/libexif
        GIT_TAG 180c1201dc494a06335b3b42bce5d4e07e6ae38c
        DEPENDS ep_zlib
        CONFIGURE_COMMAND
           cd <SOURCE_DIR> && autoreconf -i && ./configure ${HOST_FLAG} --prefix ${CMAKE_BINARY_DIR}/sysroot
        BUILD_COMMAND ${Make_EXECUTABLE} all
        INSTALL_COMMAND ${Make_EXECUTABLE} install
        BUILD_IN_SOURCE true
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)