include(ExternalProject)

list(APPEND DEPENDENCIES ep_tiff)
ExternalProject_Add(ep_tiff
    URL http://download.osgeo.org/libtiff/tiff-4.6.0.tar.gz
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
        -DCMAKE_PREFIX_PATH=${CMAKE_BINARY_DIR}/fakeroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        DEPENDS
        ep_zlib
        ep_mozjpeg
        ep_webp
)