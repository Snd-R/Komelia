include(ExternalProject)

ExternalProject_Add(ep_spng
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libpng
        DEPENDS ep_zlib
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DZLIB_ROOT:STRING=${THIRD_PARTY_LIB_PATH}
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)