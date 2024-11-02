include(ExternalProject)

ExternalProject_Add(ep_iconv
    URL https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz
    CONFIGURE_COMMAND
        <SOURCE_DIR>/configure ${HOST_FLAG}
        --prefix ${CMAKE_BINARY_DIR}/sysroot
        --enable-extra-encodings
    BUILD_COMMAND
        ${Make_EXECUTABLE}
    INSTALL_COMMAND
        ${Make_EXECUTABLE} install
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)