#!/bin/bash
set -e

rm -rf ./build
mkdir -p ./build/fakeroot
cd ./build

wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 \
        https://github.com/microsoft/onnxruntime/releases/download/v1.19.0/onnxruntime-linux-x64-1.19.0.tgz \
        && tar -xzvf onnxruntime-linux-x64-1.19.0.tgz --strip-components=1 -C ./fakeroot

export PKG_CONFIG_PATH="$(readlink -f .)/fakeroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/fakeroot/lib/pkgconfig"

cmake .. -G Ninja  \
        -DCMAKE_BUILD_TYPE=Release \
        -DVULKAN_GPU_ENUMERATION=OFF \
        -DROCM_GPU_ENUMERATION=OFF \
        -DDXGI_GPU_ENUMERATION=ON \
        -DCUDA_GPU_ENUMERATION=ON \
        -DCUDA_CUSTOM_PATH="$CUDA_CUSTOM_PATH" \
        -DSKIA_CUSTOM_PATH="$SKIA_CUSTOM_PATH"

cmake --build . -j $(nproc)

for lib in fakeroot/lib/*so; do
    strip $lib
done
