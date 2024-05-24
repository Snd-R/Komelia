#!/bin/bash
set -e

rm -rf ./build-w64
mkdir -p ./build-w64
cd ./build-w64

cmake -G Ninja \
       	-DCMAKE_BUILD_TYPE=Release \
       	-DCMAKE_TOOLCHAIN_FILE=toolchain-mingw-w64-x86_64.cmake ..

cmake --build . -j $(nproc)

cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libstdc++-6.dll ./fakeroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgcc_s_seh-1.dll ./fakeroot/bin
cp /usr/x86_64-w64-mingw32/lib/libwinpthread-1.dll ./fakeroot/bin
