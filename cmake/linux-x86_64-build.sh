#!/bin/bash
set -e

rm -rf ./cmake/build
mkdir -p ./cmake/build/sysroot
cd ./cmake/build

export PKG_CONFIG_PATH="$(readlink -f .)/sysroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/sysroot/lib/pkgconfig"

cmake ../.. -G Ninja \
        -DCMAKE_BUILD_TYPE=Release \
        -DROCM_GPU_ENUMERATION=ON \
        -DCUDA_GPU_ENUMERATION=ON \
        -DVULKAN_GPU_ENUMERATION=ON \
        -DDXGI_GPU_ENUMERATION=OFF \
        -DCUDA_CUSTOM_PATH="$CUDA_CUSTOM_PATH" \
        -DROCM_CUSTOM_PATH="$ROCM_CUSTOM_PATH" \
        -DWEBVIEW_USE_COMPAT_MINGW=OFF

cmake --build . -j $(nproc)

for lib in sysroot/lib/*so; do
    strip $lib
done
