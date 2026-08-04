include(ExternalProject)

ExternalProject_Add(ep_highway
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/highway
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DHWY_ENABLE_TESTS=OFF
            -DHWY_ENABLE_CONTRIB=OFF
            -DHWY_ENABLE_EXAMPLES=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)