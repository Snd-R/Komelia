include(ExternalProject)

ExternalProject_Add(ep_tiff
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libtiff
        DEPENDS ep_zlib ep_jxl ep_webp
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -Djbig=OFF
            -Dlzma=OFF
            -Dlerc=OFF
            -Dlibdeflate=OFF
            -Dcxx=OFF
            -Dtiff-tests=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)