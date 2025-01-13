include(ExternalProject)

ExternalProject_Add(ep_spng
        GIT_REPOSITORY https://github.com/randy408/libspng.git
        GIT_TAG v0.7.4
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        DEPENDS ep_zlib
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DZLIB_ROOT:STRING=${CMAKE_BINARY_DIR}/sysroot
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)