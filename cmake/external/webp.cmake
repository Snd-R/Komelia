include(ExternalProject)

ExternalProject_Add(ep_webp
        GIT_REPOSITORY https://chromium.googlesource.com/webm/libwebp
        GIT_TAG 1.4.0
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWEBP_BUILD_VWEBP=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)