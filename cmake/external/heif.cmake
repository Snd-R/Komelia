include(ExternalProject)

ExternalProject_Add(ep_heif
        GIT_REPOSITORY https://github.com/strukturag/libheif.git
        GIT_TAG v1.19.1
        DEPENDS ep_dav1d ep_de265 ep_webp ep_zlib ep_brotli
        CMAKE_ARGS ${EP_CMAKE_ARGS}
            -DWITH_DAV1D=ON
            -DWITH_LIBDE265=ON
            -DWITH_GDK_PIXBUF=OFF
            -DWITH_X265=OFF
            -DWITH_AOM_DECODER=OFF
            -DWITH_AOM_ENCODER=OFF
            -DWITH_SvtEnc=OFF
            -DWITH_FFMPEG_DECODER=OFF
            -DWITH_FFMPEG_ENCODER=OFF
            -DWITH_JPEG_DECODER=OFF
            -DWITH_JPEG_ENCODER=OFF
            -DWITH_KVAZAAR=OFF
            -DWITH_OpenJPEG_DECODER=OFF
            -DWITH_OpenJPEG_ENCODER=OFF
            -DWITH_DAV1D_PLUGIN=OFF
            -DWITH_LIBDE265_PLUGIN=OFF
            -DWITH_EXAMPLES=OFF
            --preset=release
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)