include(ExternalProject)

ExternalProject_Add(ep_komelia
    DOWNLOAD_COMMAND ""
    UPDATE_COMMAND ""
    DEPENDS ep_vips
    SOURCE_DIR ${PROJECT_SOURCE_DIR}/komelia_lib
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/fakeroot
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
        -DCMAKE_FIND_ROOT_PATH=${CMAKE_FIND_ROOT_PATH}
        -DCMAKE_SYSROOT=${CMAKE_SYSROOT}
        -DONNXRUNTIME_PATH=${THIRD_PARTY_LIB_PATH}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF
        -DVULKAN_GPU_ENUMERATION=${VULKAN_GPU_ENUMERATION}
        -DCUDA_GPU_ENUMERATION=${CUDA_GPU_ENUMERATION}
        -DROCM_GPU_ENUMERATION=${ROCM_GPU_ENUMERATION}
        -DDXGI_GPU_ENUMERATION=${DXGI_GPU_ENUMERATION}
        -DCUDA_CUSTOM_PATH=${CUDA_CUSTOM_PATH}
        -DSKIA_CUSTOM_PATH=${SKIA_CUSTOM_PATH}
    USES_TERMINAL_DOWNLOAD true
    USES_TERMINAL_BUILD true
)