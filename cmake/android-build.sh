#!/bin/bash

set -e

if [ -z "${ANDROID_SDK_PATH}" ]; then
    echo "\$ANDROID_SDK_PATH env variable is not set"
    exit
fi

if [ -z "${ANDROID_NDK_PATH}" ]; then
    echo "\$ANDROID_NDK_HOME env variable is not set"
    exit
fi

if [ -z "$1" ] ; then
  echo "please provide target architecture as first argument (x86, x86_64, armv7a, aarch64)"
   exit
fi

TOOLCHAIN_PATH=$ANDROID_NDK_PATH/toolchains/llvm/prebuilt/linux-x86_64/

ARCH=$1
ANDROID_ABI=""
MESON_CROSS_CPU_FAMILY=""
MESON_CROSS_CPU=""
CLANG_C_PATH=""
CLANG_CPP_PATH=""
CLANG_LIBOMP_PATH=""
case "$1" in
    armv7a)
            echo "Build script target arch: armv7a"
            ANDROID_ABI="armeabi-v7a"
            MESON_CROSS_CPU_FAMILY="armv7a"
            MESON_CROSS_CPU="arm"
            CLANG_C_PATH="${TOOLCHAIN_PATH}/bin/armv7a-linux-androideabi26-clang"
            CLANG_CPP_PATH="${TOOLCHAIN_PATH}/bin/armv7a-linux-androideabi26-clang++"
            CLANG_LIBOMP_PATH="${TOOLCHAIN_PATH}/lib/clang/19/lib/linux/arm/libomp.so"
            ;;
    aarch64)
            echo "Build script target arch: aarch64"
            ANDROID_ABI=arm64-v8a
            MESON_CROSS_CPU_FAMILY=$ARCH
            MESON_CROSS_CPU=$ARCH
            CLANG_C_PATH="${TOOLCHAIN_PATH}/bin/aarch64-linux-android26-clang"
            CLANG_CPP_PATH="${TOOLCHAIN_PATH}/bin/aarch64-linux-android26-clang++"
            CLANG_LIBOMP_PATH="${TOOLCHAIN_PATH}/lib/clang/19/lib/linux/aarch64/libomp.so"
            ;;
    x86)
            echo "Build script target arch: x86"
            ANDROID_ABI=$ARCH
            MESON_CROSS_CPU_FAMILY=$ARCH
            MESON_CROSS_CPU=$ARCH
            CLANG_C_PATH="${TOOLCHAIN_PATH}/bin/i686-linux-android26-clang"
            CLANG_CPP_PATH="${TOOLCHAIN_PATH}/bin/i686-linux-android26-clang++"
            CLANG_LIBOMP_PATH="${TOOLCHAIN_PATH}/lib/clang/19/lib/linux/i386/libomp.so"
            ;;
    x86_64)
            echo "Build script target arch: x86_64"
            ANDROID_ABI=$ARCH
            MESON_CROSS_CPU_FAMILY=$ARCH
            MESON_CROSS_CPU=$ARCH
            CLANG_C_PATH="${TOOLCHAIN_PATH}/bin/x86_64-linux-android26-clang"
            CLANG_CPP_PATH="${TOOLCHAIN_PATH}/bin/x86_64-linux-android26-clang++"
            CLANG_LIBOMP_PATH="${TOOLCHAIN_PATH}/lib/clang/19/lib/linux/x86_64/libomp.so"
            ;;
    *)
            echo "Build script unsupported architecture $1"
            exit;;
esac

rm -rf ./cmake/build-android-"$ARCH"
mkdir -p ./cmake/build-android-"$ARCH"/sysroot
cd ./cmake/build-android-"$ARCH"


SYSROOT="$(readlink -f .)/sysroot"
export PKG_CONFIG_DIR=""
export PKG_CONFIG_LIBDIR="${SYSROOT}/lib/pkgconfig"
export PKG_CONFIG_PATH="${SYSROOT}/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="${SYSROOT}/lib/pkgconfig"

export ANDROID_PLATFORM=26
export AR=$TOOLCHAIN_PATH/bin/llvm-ar
export CC=$CLANG_C_PATH
export AS=$CC
export CXX=$CLANG_CPP_PATH
export LD=$TOOLCHAIN_PATH/bin/ld
export RANLIB=$TOOLCHAIN_PATH/bin/llvm-ranlib
export STRIP=$TOOLCHAIN_PATH/bin/llvm-strip

cat << EOF > "android-$ARCH-cross_file.txt"
[host_machine]
system = 'android'
cpu_family = '$MESON_CROSS_CPU_FAMILY'
cpu = '$MESON_CROSS_CPU'
endian = 'little'

[built-in options]
c_args = ['-I$SYSROOT/include', '-Wno-error=format-nonliteral']
cpp_args = ['-I$SYSROOT/include', '-Wno-error=format-nonliteral']
c_link_args = ['-L$SYSROOT/lib']
cpp_link_args = ['-L$SYSROOT/lib']

[binaries]
c = '$CLANG_C_PATH'
cpp = '$CLANG_CPP_PATH'
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

cmake ../.. -G Ninja \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_SYSTEM_NAME=Android \
    -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_PATH/build/cmake/android.toolchain.cmake" \
    -DCMAKE_PREFIX_PATH="${SYSROOT}" \
    -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" \
    -DMESON_CROSS_FILE="$(readlink -f ./android-"$ARCH"-cross_file.txt)" \
    -DANDROID_ABI="$ANDROID_ABI" \
    -DANDROID_PLATFORM=26 \
    -DANDROID_SDK_PATH="${ANDROID_SDK_PATH}" \
    -DANDROID_NDK_PATH="${ANDROID_NDK_PATH}" \
    -DHOST_FLAG=--host="$ARCH"-linux-android

cmake --build . -j $(nproc)

cp "$CLANG_LIBOMP_PATH" ./sysroot/lib
for lib in sysroot/lib/*so; do
    [[ -f $lib && ! -h $lib ]] && "$TOOLCHAIN_PATH"/bin/llvm-strip "$lib"
done