include(ExternalProject)

ExternalProject_Add(ep_highway
        GIT_REPOSITORY https://github.com/google/highway
        GIT_TAG 1.2.0
        #GIT_TAG ee098a4285a0b1347bb2af797534d62c9fa5d390
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DHWY_ENABLE_TESTS=OFF
            -DHWY_ENABLE_CONTRIB=OFF
            -DHWY_ENABLE_EXAMPLES=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)