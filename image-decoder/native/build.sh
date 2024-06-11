#!/bin/bash
set -e

rm -rf ./build
mkdir -p ./build
cd ./build

export PKG_CONFIG_PATH="$(readlink -f .)/fakeroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/fakeroot/lib/pkgconfig"

cmake -G Ninja -DCMAKE_BUILD_TYPE=Release ..
cmake --build . -j $(nproc)

for lib in fakeroot/lib/*so; do
    strip $lib
done
