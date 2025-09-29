include(ExternalProject)

ExternalProject_Add(ep_de265
        GIT_REPOSITORY https://github.com/strukturag/libde265.git
        GIT_TAG v1.0.16
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DENABLE_SDL=OFF
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)