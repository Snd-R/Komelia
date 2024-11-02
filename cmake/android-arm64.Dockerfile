FROM ubuntu:24.04

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    build-essential \
    cmake \
    ninja-build \
    meson \
    nasm \
    autoconf \
    libtool \
    texinfo \
    pkg-config \
    libglib2.0-dev \
    unzip \
    wget \
    git

RUN wget --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 0 -O ndk.zip \
    https://dl.google.com/android/repository/android-ndk-r26d-linux.zip \
    && unzip ndk.zip \
    && mv android-ndk-r26d ndk \
    && rm -rf ndk.zip

USER 1000:1000
WORKDIR build
ENV NDK_PATH=/ndk/
ENTRYPOINT ["./cmake/build-android-arm64.sh"]
