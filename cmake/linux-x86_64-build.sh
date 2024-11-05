#!/bin/bash
set -e

rm -rf ./cmake/build
mkdir -p ./cmake/build/sysroot
cd ./cmake/build

wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 \
        https://github.com/microsoft/onnxruntime/releases/download/v1.18.1/onnxruntime-linux-x64-1.18.1.tgz \
        && tar -xzvf onnxruntime-linux-x64-1.18.1.tgz --strip-components=1 -C ./sysroot

export PKG_CONFIG_PATH="$(readlink -f .)/sysroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/sysroot/lib/pkgconfig"

cmake .. -G Ninja  \
        -DCMAKE_BUILD_TYPE=Release \
        -DVULKAN_GPU_ENUMERATION=OFF \
        -DROCM_GPU_ENUMERATION=OFF \
        -DDXGI_GPU_ENUMERATION=ON \
        -DCUDA_GPU_ENUMERATION=ON \
        -DCUDA_CUSTOM_PATH="$CUDA_CUSTOM_PATH" \
        -DSKIA_CUSTOM_PATH="$SKIA_CUSTOM_PATH" \
        -DWEBVIEW_USE_COMPAT_MINGW=OFF

cmake --build . -j $(nproc)

for lib in sysroot/lib/*so; do
    strip $lib
done
