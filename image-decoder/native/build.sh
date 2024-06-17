#!/bin/bash
set -e

rm -rf ./build
mkdir -p ./build/fakeroot
cd ./build

wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 \
        https://github.com/microsoft/onnxruntime/releases/download/v1.18.0/onnxruntime-linux-x64-1.18.0.tgz \
        && tar -xzvf onnxruntime-linux-x64-1.18.0.tgz --strip-components=1 -C ./fakeroot

export PKG_CONFIG_PATH="$(readlink -f .)/fakeroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/fakeroot/lib/pkgconfig"

cmake -G Ninja -DCMAKE_BUILD_TYPE=Release ..
cmake --build . -j $(nproc)

for lib in fakeroot/lib/*so; do
    strip $lib
done
