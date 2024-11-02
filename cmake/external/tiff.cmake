include(ExternalProject)

ExternalProject_Add(ep_tiff
    URL http://download.osgeo.org/libtiff/tiff-4.6.0.tar.gz
    DEPENDS ep_zlib ep_jxl ep_webp
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/sysroot
        -DCMAKE_PREFIX_PATH=${CMAKE_BINARY_DIR}/sysroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -Djbig=OFF
        -Dlzma=OFF
        -Dlerc=OFF
        -Dlibdeflate=OFF
        -Dcxx=OFF
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)