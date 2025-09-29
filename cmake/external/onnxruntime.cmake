include(ExternalProject)
if (WIN32)
    # Windows version can only be built with MSVC. Use prebuilt binaries instead
else ()
    if (ANDROID)
        # CPU ONLY
        # WebGPU seem to be slower than CPU inference
        # and can hang the whole system UI until processing is done
        set(ONNXRUNTIME_BUILD_PARAMS "")
    else ()
        # WebGPU with static Dawn
        set(ONNXRUNTIME_BUILD_PARAMS
                -Donnxruntime_USE_WEBGPU=ON
                -Donnxruntime_WGSL_TEMPLATE=static
                -Donnxruntime_BUILD_DAWN_SHARED_LIBRARY=OFF
        )

    endif ()

    ExternalProject_Add(ep_onnxruntime
            GIT_REPOSITORY https://github.com/microsoft/onnxruntime
            GIT_TAG v1.23.0
            GIT_PROGRESS 1
            SOURCE_SUBDIR cmake
            CMAKE_ARGS
                ${EP_CMAKE_ARGS}
                --compile-no-warning-as-error
                -Donnxruntime_BUILD_SHARED_LIB=ON
                -Donnxruntime_BUILD_UNIT_TESTS=OFF
                -DONNX_BUILD_SHARED_LIBS=OFF
                -DBUILD_SHARED_LIBS=OFF
                ${ONNXRUNTIME_BUILD_PARAMS}
            USES_TERMINAL_DOWNLOAD true
            USES_TERMINAL_BUILD true
    )
endif ()