include(ExternalProject)

ExternalProject_Add(ep_highway
        GIT_REPOSITORY https://github.com/google/highway
        GIT_TAG 1.3.0
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DHWY_ENABLE_TESTS=OFF
            -DHWY_ENABLE_CONTRIB=OFF
            -DHWY_ENABLE_EXAMPLES=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)