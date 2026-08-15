include(ExternalProject)

ExternalProject_Add(ep_exif
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libexif
        DEPENDS ep_zlib
        PATCH_COMMAND git clean -dfx
        CONFIGURE_COMMAND
           cd <SOURCE_DIR> && autoreconf -i && ./configure ${HOST_FLAG} --prefix ${CMAKE_BINARY_DIR}/sysroot
        BUILD_COMMAND ${Make_EXECUTABLE} all
        INSTALL_COMMAND ${Make_EXECUTABLE} install
        BUILD_IN_SOURCE true
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)