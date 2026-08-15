include(ExternalProject)

ExternalProject_Add(ep_de265
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libde265
        PATCH_COMMAND git clean -dfx
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DENABLE_SDL=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)