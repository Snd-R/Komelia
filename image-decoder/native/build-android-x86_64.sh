#!/bin/bash

set -e

if [ -z "${NDK_PATH}" ]; then
    echo "\$NDK_PATH env variable is not set"
    exit
fi

TOOLCHAIN_PATH=$NDK_PATH/toolchains/llvm/prebuilt/linux-x86_64/
echo "$NDK_PATH/build/cmake/android.toolchain.cmake"

rm -rf ./build-android-x86_64
mkdir -p ./build-android-x86_64/fakeroot
cd ./build-android-x86_64


SYSROOT="$(readlink -f .)/fakeroot"
export PKG_CONFIG_DIR=""
export PKG_CONFIG_LIBDIR="${SYSROOT}/lib/pkgconfig"
export PKG_CONFIG_PATH="${SYSROOT}/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="${SYSROOT}/lib/pkgconfig"

export ANDROID_PLATFORM=26
export AR=$TOOLCHAIN_PATH/bin/llvm-ar
export CC=$TOOLCHAIN_PATH/bin/x86_64-linux-android26-clang
export AS=$CC
export CXX=$TOOLCHAIN_PATH/bin/x86_64-linux-android26-clang++
export LD=$TOOLCHAIN_PATH/bin/ld
export RANLIB=$TOOLCHAIN_PATH/bin/llvm-ranlib
export STRIP=$TOOLCHAIN_PATH/bin/llvm-strip

cat << EOF > "android-x86_64-cross_file.txt"
[host_machine]
system = 'android'
cpu_family = 'x86_64'
cpu = 'x86_64'
endian = 'little'

[built-in options]
c_args = ['-I$SYSROOT/include', '-Wno-error=format-nonliteral']
cpp_args = ['-I$SYSROOT/include', '-Wno-error=format-nonliteral']
c_link_args = ['-L$SYSROOT/lib']
cpp_link_args = ['-L$SYSROOT/lib']

[binaries]
c = '$TOOLCHAIN_PATH/bin/x86_64-linux-android26-clang'
cpp = '$TOOLCHAIN_PATH/bin/x86_64-linux-android26-clang++'
ar = '$TOOLCHAIN_PATH/bin/llvm-ar'
strip = '$TOOLCHAIN_PATH/bin/llvm-strip'
ranlib = '$TOOLCHAIN_PATH/bin/llvm-ranlib'
c_ld = '$TOOLCHAIN_PATH/bin/ld'
pkg-config = '$(which pkg-config)'

[cmake]
CMAKE_BUILD_WITH_INSTALL_RPATH     = 'ON'
CMAKE_FIND_ROOT_PATH_MODE_PROGRAM  = 'NEVER'
CMAKE_FIND_ROOT_PATH_MODE_LIBRARY  = 'ONLY'
CMAKE_FIND_ROOT_PATH_MODE_INCLUDE  = 'ONLY'
CMAKE_FIND_ROOT_PATH_MODE_PACKAGE  = 'ONLY'
EOF

cmake -G Ninja \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_SYSTEM_NAME=Android \
    -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
    -DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF \
    -DCMAKE_PREFIX_PATH="${SYSROOT}" \
    -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" \
    -DMESON_CROSS_FILE="$(readlink -f ./android-x86_64-cross_file.txt)" \
    -DANDROID_ABI=x86_64 \
    -DANDROID_PLATFORM=26 \
    -DHOST_FLAG=--host=x86_64-linux-android \
    ..
cmake --build . -j $(nproc)

cp "${TOOLCHAIN_PATH}/lib/clang/17/lib/linux/x86_64/libomp.so" ./fakeroot/lib

for lib in fakeroot/lib/*so; do
    [[ -f $lib && ! -h $lib ]] && "$TOOLCHAIN_PATH"/bin/llvm-strip "$lib"
done