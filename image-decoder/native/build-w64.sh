#!/bin/bash
set -e

rm -rf ./build-w64
mkdir -p ./build-w64
cd ./build-w64

export PKG_CONFIG_PATH="$(readlink -f .)/fakeroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/fakeroot/lib/pkgconfig"

cmake -G Ninja \
       	-DCMAKE_BUILD_TYPE=Release \
       	-DCMAKE_TOOLCHAIN_FILE=toolchain-mingw-w64-x86_64.cmake .. \
       	-DMESON_CROSS_FILE=$(readlink -f ../cmake/w64.cross-file.txt)

cmake --build . -j $(nproc)

cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libstdc++-6.dll ./fakeroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgcc_s_seh-1.dll ./fakeroot/bin
cp /usr/x86_64-w64-mingw32/lib/libwinpthread-1.dll ./fakeroot/bin

for lib in fakeroot/bin/*dll; do
  x86_64-w64-mingw32-strip $lib
done
