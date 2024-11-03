include(ExternalProject)

ExternalProject_Add(ep_de265
        GIT_REPOSITORY https://github.com/strukturag/libde265.git
        GIT_TAG v1.0.15
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DENABLE_SDL=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)