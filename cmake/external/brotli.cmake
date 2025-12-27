include(ExternalProject)

ExternalProject_Add(ep_brotli
        GIT_REPOSITORY https://github.com/google/brotli
        GIT_TAG v1.2.0
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DBROTLI_DISABLE_TESTS=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)
