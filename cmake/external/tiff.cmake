include(ExternalProject)

ExternalProject_Add(ep_tiff
        URL http://download.osgeo.org/libtiff/tiff-4.6.0.tar.gz
        DEPENDS ep_zlib ep_jxl ep_webp
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -Djbig=OFF
            -Dlzma=OFF
            -Dlerc=OFF
            -Dlibdeflate=OFF
            -Dcxx=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)