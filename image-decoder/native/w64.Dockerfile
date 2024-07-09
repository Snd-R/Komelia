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
    mingw-w64 \
    binutils-mingw-w64 \
    mingw-w64-tools \
    pkg-config \
    libglib2.0-dev \
    unzip \
    wget \
    git

RUN update-alternatives --set x86_64-w64-mingw32-g++ /usr/bin/x86_64-w64-mingw32-g++-posix

RUN wget --retry-connrefused --waitretry=1 \
	--read-timeout=20 --timeout=15 -t 0 -O jdk.zip \
        https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%2B9/OpenJDK21U-jdk_x64_windows_hotspot_21.0.3_9.zip \
    && unzip jdk.zip \
    && mv jdk-21.0.3+9 jdk \
    && rm -rf jdk.zip

USER 1000:1000
WORKDIR build

ENV JAVA_HOME=/jdk/

ENTRYPOINT ["./build-w64.sh"]
