FROM gcc:14-bookworm

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

RUN mkdir /rocm_hip_download && mkdir /rocm && cd /rocm_hip_download \
    && wget -q --show-progress https://repo.radeon.com/rocm/apt/7.0.1/pool/main/h/hip-dev/hip-dev_7.0.51831.70001-42~24.04_amd64.deb \
    && wget -q --show-progress https://repo.radeon.com/rocm/apt/7.0.1/pool/main/h/hip-runtime-amd/hip-runtime-amd_7.0.51831.70001-42~24.04_amd64.deb \
    && ar x hip-dev_7.0.51831.70001-42~24.04_amd64.deb \
    && tar xf data.tar.gz \
    && ar x hip-runtime-amd_7.0.51831.70001-42~24.04_amd64.deb \
    && tar xf data.tar.gz \
    && \cp -rf ./opt/rocm-7.0.1/* /rocm \
    && cd / && rm -rf /rocm_hip_download

RUN echo 'deb https://deb.debian.org/debian bookworm-backports main' >> /etc/apt/sources.list

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
    libwebkit2gtk-4.1-dev \
    libxrandr-dev \
    libxinerama-dev \
    libxcursor-dev \
    mesa-common-dev \
    libx11-xcb-dev \
    -t bookworm-backports

RUN pip install meson --break-system-packages

RUN curl -Lo node.tar.gz https://nodejs.org/dist/v24.8.0/node-v24.8.0-linux-x64.tar.gz \
      && echo "daf68404b478b4c3616666580d02500a24148c0f439e4d0134d65ce70e90e655 node.tar.gz" | sha256sum -c - \
      && tar xzf node.tar.gz --strip-components=1 -C /usr/local/

RUN mkdir /.npm && chown -R 1000:1000 /.npm

USER 1000:1000
WORKDIR build

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
ENV CUDA_CUSTOM_PATH=/cuda/
ENV ROCM_CUSTOM_PATH=/rocm/

ENTRYPOINT ["./cmake/linux-x86_64-build.sh"]
