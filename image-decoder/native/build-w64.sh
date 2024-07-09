#!/bin/bash
set -e

rm -rf ./build-w64
mkdir -p ./build-w64/fakeroot
mkdir -p ./build-w64/fakeroot/include
mkdir -p ./build-w64/fakeroot/lib
cd ./build-w64

wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 \
        https://github.com/microsoft/onnxruntime/releases/download/v1.18.0/Microsoft.ML.OnnxRuntime.DirectML.1.18.0.zip \
        && unzip Microsoft.ML.OnnxRuntime.DirectML.1.18.0.zip -d onnxruntime-win-x64 \
        && mv ./onnxruntime-win-x64/build/native/include/* ./fakeroot/include \
        && mv ./onnxruntime-win-x64/runtimes/win-x64/native/* ./fakeroot/lib

patch ./fakeroot/include/onnxruntime_c_api.h ../w64-mingw_onnxruntime_c_api.h.patch

export PKG_CONFIG_PATH="$(readlink -f .)/fakeroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/fakeroot/lib/pkgconfig"

cmake -G Ninja \
       	-DCMAKE_BUILD_TYPE=Release \
       	-DCMAKE_TOOLCHAIN_FILE=w64-toolchain-mingw-x86_64.cmake .. \
       	-DMESON_CROSS_FILE="$(readlink -f ../w64-mingw-x86_64-cross_file.txt)" \
        -DVULKAN_GPU_ENUMERATION=OFF \
        -DCUDA_GPU_ENUMERATION=OFF \
        -DROCM_GPU_ENUMERATION=OFF \
        -DDXGI_GPU_ENUMERATION=ON

cmake --build . -j $(nproc)

cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libstdc++-6.dll ./fakeroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgcc_s_seh-1.dll ./fakeroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgomp-1.dll ./fakeroot/bin
cp /usr/x86_64-w64-mingw32/lib/libwinpthread-1.dll ./fakeroot/bin

for lib in fakeroot/bin/*dll; do
  x86_64-w64-mingw32-strip $lib
done
