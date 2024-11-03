include(ExternalProject)

ExternalProject_Add(ep_mozjpeg
        GIT_REPOSITORY https://github.com/mozilla/mozjpeg.git
        GIT_TAG v4.1.5
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DPNG_SUPPORTED=OFF
            -DCMAKE_POLICY_DEFAULT_CMP0057:STRING=NEW
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)