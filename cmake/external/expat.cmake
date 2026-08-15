include(ExternalProject)

ExternalProject_Add(ep_expat
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libexpat
        PATCH_COMMAND git clean -dfx
        CMAKE_ARGS ${EP_CMAKE_ARGS}
        SOURCE_SUBDIR expat
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)