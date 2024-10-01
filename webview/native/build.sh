#!/bin/bash
set -e

rm -rf ./build
mkdir ./build
cd ./build

cmake .. -G Ninja  \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}

cmake --build . -j $(nproc)

for lib in lib/*so; do
    strip $lib
done
