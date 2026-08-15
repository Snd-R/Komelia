include(ExternalProject)

ExternalProject_Add(ep_brotli
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/brotli
        PATCH_COMMAND git clean -dfx
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DBROTLI_DISABLE_TESTS=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)
