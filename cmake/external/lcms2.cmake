include(ExternalProject)

ExternalProject_Add(ep_lcms2
        GIT_REPOSITORY https://github.com/mm2/Little-CMS
        GIT_TAG lcms2.17
        GIT_SHALLOW 1
        GIT_PROGRESS 1
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