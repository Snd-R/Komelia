include(ExternalProject)

ExternalProject_Add(ep_ffi
    GIT_REPOSITORY      https://github.com/libffi/libffi.git
    GIT_TAG             v3.4.6
    BUILD_IN_SOURCE 1
    CONFIGURE_COMMAND
        <SOURCE_DIR>/autogen.sh && <SOURCE_DIR>/configure ${HOST_FLAG}
        --disable-exec-static-tramp
        --disable-multi-os-directory
        --disable-static
        --enable-pax_emutramp
        --prefix ${CMAKE_BINARY_DIR}/sysroot
    BUILD_COMMAND
        ${Make_EXECUTABLE}
    INSTALL_COMMAND
        ${Make_EXECUTABLE} install
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)