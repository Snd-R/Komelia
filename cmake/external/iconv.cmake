include(ExternalProject)

ExternalProject_Add(ep_iconv
        URL https://ftpmirror.gnu.org/libiconv/libiconv-1.18.tar.gz
        CONFIGURE_COMMAND <SOURCE_DIR>/configure ${HOST_FLAG} --prefix ${CMAKE_BINARY_DIR}/sysroot
            --enable-extra-encodings
        BUILD_COMMAND ${Make_EXECUTABLE}
        INSTALL_COMMAND ${Make_EXECUTABLE} install
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)