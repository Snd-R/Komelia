include(ExternalProject)

ExternalProject_Add(ep_webp
        GIT_REPOSITORY https://chromium.googlesource.com/webm/libwebp
        GIT_TAG 1.4.0
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWEBP_BUILD_VWEBP=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)