#!/bin/bash
set -e

rm -rf ./cmake/build-w64
mkdir -p ./cmake/build-w64/sysroot
mkdir -p ./cmake/build-w64/sysroot/include
mkdir -p ./cmake/build-w64/sysroot/lib
cd ./cmake/build-w64

mkdir onnxruntime-win-x64
wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 \
        https://globalcdn.nuget.org/packages/microsoft.ml.onnxruntime.directml.1.21.0.nupkg \
        && unzip microsoft.ml.onnxruntime.directml.1.21.0.nupkg -d onnxruntime-win-x64 \
        && mv ./onnxruntime-win-x64/build/native/include/* ./sysroot/include \
        && mv ./onnxruntime-win-x64/runtimes/win-x64/native/* ./sysroot/lib

patch ./sysroot/include/onnxruntime_c_api.h ../windows-x64-mingw_onnxruntime_c_api.h.patch

export PKG_CONFIG_PATH="$(readlink -f .)/sysroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/sysroot/lib/pkgconfig"

TOOLCHAIN_FILE="$(readlink -f ../windows-x64-toolchain-mingw-x86_64.cmake)"
CROSS_FILE="$(readlink -f ../windows-x64-mingw-x86_64-cross_file.txt)"

cmake ../.. -G Ninja \
       	-DCMAKE_BUILD_TYPE=Release \
       	-DCMAKE_TOOLCHAIN_FILE=$TOOLCHAIN_FILE  \
       	-DMESON_CROSS_FILE=$CROSS_FILE \
        -DROCM_GPU_ENUMERATION=OFF \
        -DDXGI_GPU_ENUMERATION=ON \
        -DCUDA_GPU_ENUMERATION=ON \
        -DWEBVIEW_USE_COMPAT_MINGW=ON \
        -DCUDA_CUSTOM_PATH="$CUDA_CUSTOM_PATH"

cmake --build . -j $(nproc)

cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libstdc++-6.dll ./sysroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgcc_s_seh-1.dll ./sysroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgomp-1.dll ./sysroot/bin
cp /usr/x86_64-w64-mingw32/lib/libwinpthread-1.dll ./sysroot/bin

for lib in sysroot/bin/*dll; do
  x86_64-w64-mingw32-strip $lib
done
