include(ExternalProject)

ExternalProject_Add(ep_webp
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libwebp
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWEBP_BUILD_VWEBP=OFF
            -DWEBP_LINK_STATIC=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)