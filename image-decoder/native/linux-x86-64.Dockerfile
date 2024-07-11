FROM gcc:12-bullseye

RUN echo 'deb https://deb.debian.org/debian bullseye-backports main' >> /etc/apt/sources.list

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    cmake \
    ninja-build \
    nasm \
    openjdk-17-jdk-headless \
    texinfo \
    gettext \
    libvulkan-dev \
    python3-pip \
    python3-packaging \
    -t bullseye-backports

RUN pip3 install meson

RUN wget --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 0 -qO - https://ftpmirror.gnu.org/autoconf/autoconf-2.71.tar.gz | tar -xvzf -\
    && mkdir -p /i \
    && cd autoconf-2.71 \
    && ./configure --prefix=/i \
    && make && make install \
    && rm -rf autoconf-2.71

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
    && cd / && rm -rf /cuda_

USER 1000:1000
WORKDIR build

ENV PATH=/i/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
ENV CUDA_CUSTOM_PATH=/cuda/

ENTRYPOINT ["./build.sh"]
