include(ExternalProject)

set(JXL_ARGS ${EP_CMAKE_ARGS}
        -DBROTLI_INCLUDE_DIR=${THIRD_PARTY_LIB_PATH}/include
        -DJPEGXL_FORCE_SYSTEM_BROTLI=ON
        -DJPEGXL_BUNDLE_LIBPNG=OFF
        -DJPEGXL_ENABLE_DOXYGEN=OFF
        -DJPEGXL_ENABLE_MANPAGES=OFF
        -DJPEGXL_ENABLE_EXAMPLES=OFF
        -DJPEGXL_ENABLE_SJPEG=OFF
        -DJPEGXL_ENABLE_OPENEXR=OFF
        -DJPEGXL_ENABLE_TRANSCODE_JPEG=OFF
        -DJPEGXL_ENABLE_BENCHMARK=OFF
        -DJPEGXL_ENABLE_TOOLS=OFF
        -DJPEGXL_ENABLE_DEVTOOLS=OFF
        -DJPEGXL_ENABLE_JPEGLI=OFF)

# fails when building with android clang toolchain
# for some reason it tries to find hwy test headers but we don't build them
if (NOT ANDROID)
    set(JXL_ARGS ${JXL_ARGS} -DJPEGXL_FORCE_SYSTEM_HWY=ON)
endif ()

ExternalProject_Add(ep_jxl
        GIT_REPOSITORY https://github.com/libjxl/libjxl
        GIT_TAG v0.11.0
        DEPENDS ep_highway ep_brotli
        CMAKE_ARGS ${JXL_ARGS}
        USES_TERMINAL_DOWNLOAD ON
        USES_TERMINAL_BUILD ON
)