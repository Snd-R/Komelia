include(ExternalProject)

ExternalProject_Add(ep_expat
        GIT_REPOSITORY https://github.com/libexpat/libexpat.git
        GIT_TAG R_2_6_2
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS ${EP_CMAKE_ARGS}
        SOURCE_SUBDIR expat
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)