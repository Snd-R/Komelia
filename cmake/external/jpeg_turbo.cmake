include(ExternalProject)

ExternalProject_Add(ep_jpeg-turbo
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libjpeg-turbo
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWITH_SYSTEM_ZLIB=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)
