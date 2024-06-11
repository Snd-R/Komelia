FROM gcc:12-bullseye

RUN echo 'deb https://deb.debian.org/debian bullseye-backports main' >> /etc/apt/sources.list

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    cmake \
    ninja-build \
    nasm \
    openjdk-17-jdk-headless \
    texinfo \
    python3-pip \
    python3-packaging \
    -t bullseye-backports \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install meson

RUN wget --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 0 -qO - https://ftpmirror.gnu.org/autoconf/autoconf-2.71.tar.gz | tar -xvzf -\
    && mkdir -p /i \
    && cd autoconf-2.71 \
    && ./configure --prefix=/i \
    && make && make install \
    && rm -rf autoconf-2.71
USER 1000:1000
WORKDIR build

ENV PATH=/i/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/

ENTRYPOINT ["./build.sh"]
