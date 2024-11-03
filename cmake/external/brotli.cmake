include(ExternalProject)

ExternalProject_Add(ep_brotli
        GIT_REPOSITORY https://github.com/google/brotli
        GIT_TAG v1.1.0
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DBROTLI_DISABLE_TESTS=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)