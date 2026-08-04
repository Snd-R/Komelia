include(ExternalProject)

ExternalProject_Add(ep_lcms2
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/Little-CMS
        DEPENDS ep_zlib
        CONFIGURE_COMMAND
            <SOURCE_DIR>/configure ${HOST_FLAG}
            --prefix ${CMAKE_BINARY_DIR}/sysroot
        BUILD_COMMAND ${Make_EXECUTABLE}
        INSTALL_COMMAND ${Make_EXECUTABLE} install
        BUILD_IN_SOURCE true
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)