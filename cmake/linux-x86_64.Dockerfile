FROM gcc:14-bookworm

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    cmake \
    ninja-build \
    nasm \
    autoconf \
    automake \
    autopoint \
    autotools-dev \
    openjdk-17-jdk \
    texinfo \
    gettext \
    libvulkan-dev \
    python3-pip \
    python3-packaging \
    pipx \
    libwebkit2gtk-4.1-dev

RUN pip install meson --break-system-packages

RUN mkdir /cuda_download && cd /cuda_download \
    && wget -q --show-progress https://developer.download.nvidia.com/compute/cuda/12.5.1/local_installers/cuda-repo-debian11-12-5-local_12.5.1-555.42.06-1_amd64.deb \
    && mkdir /cuda \
    && ar x cuda-repo-debian11-12-5-local_12.5.1-555.42.06-1_amd64.deb \
    && tar xf data.tar.xz \
    && mv ./var/cuda-repo-debian11-12-5-local/cuda-crt-12-5_12.5.82-1_amd64.deb . \
    && mv ./var/cuda-repo-debian11-12-5-local/cuda-cudart-12-5_12.5.82-1_amd64.deb . \
    && mv ./var/cuda-repo-debian11-12-5-local/cuda-cudart-dev-12-5_12.5.82-1_amd64.deb . \
    && ar x cuda-crt-12-5_12.5.82-1_amd64.deb \
    && tar xf data.tar.xz \
    && ar x cuda-cudart-12-5_12.5.82-1_amd64.deb \
    && tar xf data.tar.xz \
    && ar x cuda-cudart-dev-12-5_12.5.82-1_amd64.deb \
    && tar xf data.tar.xz \
    && \cp -rf ./usr/local/cuda-12.5/targets/x86_64-linux/* /cuda \
    && cd / && rm -rf /cuda_download

RUN mkdir /skia && cd /skia \
    && wget -q --show-progress https://github.com/JetBrains/skia-pack/releases/download/m126-1d69d9b-2/Skia-m126-1d69d9b-2-linux-Release-x64.zip \
    && unzip Skia-m126-1d69d9b-2-linux-Release-x64.zip \
    && mkdir ./lib && mv ./out/Release-linux-x64/* ./lib \
    && rm -rf Skia-m126-1d69d9b-2-linux-Release-x64.zip

USER 1000:1000
WORKDIR build

ENV PATH=/i/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
ENV CUDA_CUSTOM_PATH=/cuda/
ENV SKIA_CUSTOM_PATH=/skia/

ENTRYPOINT ["./cmake/linux-x86_64-build.sh"]
