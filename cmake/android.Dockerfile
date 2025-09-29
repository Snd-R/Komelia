FROM ubuntu:24.04

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    openjdk-17-jdk-headless \
    python3-dev \
    python3-numpy \
    python3-pip \
    python3-setuptools \
    python3-wheel \
    build-essential \
    cmake \
    ninja-build \
    meson \
    nasm \
    autoconf \
    automake \
    autopoint \
    autotools-dev \
    libtool \
    texinfo \
    pkg-config \
    libglib2.0-dev \
    libgtest-dev \
    unzip \
    wget \
    git \
    aria2 \
    curl

ENV ANDROID_SDK_PATH=/android-sdk
ENV NDK_VERSION=28.0.13004108
ENV ANDROID_NDK_PATH=${ANDROID_SDK_PATH}/ndk/${NDK_VERSION}

RUN aria2c -q -d /tmp -o cmdline-tools.zip \
  --checksum=sha-256=0bebf59339eaa534f4217f8aa0972d14dc49e7207be225511073c661ae01da0a \
  https://dl.google.com/android/repository/commandlinetools-linux-9123335_latest.zip && \
  unzip /tmp/cmdline-tools.zip -d /tmp/cmdline-tools && \
  mkdir -p ${ANDROID_SDK_PATH}/cmdline-tools && \
  mv /tmp/cmdline-tools/cmdline-tools ${ANDROID_SDK_PATH}/cmdline-tools/latest

RUN yes | ${ANDROID_SDK_PATH}/cmdline-tools/latest/bin/sdkmanager --licenses
RUN ${ANDROID_SDK_PATH}/cmdline-tools/latest/bin/sdkmanager --install \
  "platforms;android-35" \
  "ndk;${NDK_VERSION}"

RUN curl -Lo node.tar.gz https://nodejs.org/dist/v24.8.0/node-v24.8.0-linux-x64.tar.gz \
      && echo "daf68404b478b4c3616666580d02500a24148c0f439e4d0134d65ce70e90e655 node.tar.gz" | sha256sum -c - \
      && tar xzf node.tar.gz --strip-components=1 -C /usr/local/

RUN mkdir /.npm && chown -R 1000:1000 /.npm

USER 1000:1000
WORKDIR /build
ENTRYPOINT ["./cmake/android-build.sh"]
