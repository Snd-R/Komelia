# Komelia - Komga media client
Currently only desktop version is available. Android version is planned

Desktop version requires java 21 to run. Non jar releases include bundled jre

## Build 
Requires jdk 21 or higher 

`./gradlew run` to launch desktop app

`./gradlew repackageuberJar` to build fat jar

Depends on libvips and its dependencies for image decoding
Check vips/native directory for cmake build details
# libvips build dependencies 
- make
- cmake
- ninja
- meson
- nasm
- autotools

run `./gradlew :vips:linuxBuild` to launch linux build using system toolchain

alternatively you can run build inside docker image `cd vips/native` `docker build -t gcc-build . ` `docker run -v .:/build gcc-build <num_of_build_jobs>`

after build run `./gradlew :vips:linuxCopyVipsLibsToClasspath` to copy built libraries to be bundled with the application
### windows cross compilation
requires mingw-w64-gcc mingw-w64-binutils 

run `./gradlew :vips:windowsBuild` then `./gradlew :vips:windowsCopyVipsLibsToClasspath` to copy libraries to be bundled with the app
