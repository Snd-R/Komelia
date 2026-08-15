include(ExternalProject)

ExternalProject_Add(ep_lcms2
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/Little-CMS
        PATCH_COMMAND git clean -dfx
        DEPENDS ep_zlib
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DLCMS2_BUILD_TOOLS=OFF
            -DLCMS2_BUILD_TESTS=OFF
            -DLCMS2_BUILD_TESTS=OFF
            -DLCMS2_BUILD_SHARED=ON
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)