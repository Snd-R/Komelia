# Komelia - Komga media client

### latest version available at https://github.com/Snd-R/Komelia/releases

## Desktop Build

make sure to clone with submodules `git clone --recurse-submodules https://github.com/Snd-R/Komelia`

Requires jdk 17 or higher

`./gradlew run` to launch desktop app

`./gradlew repackageUberJar` package jar for current OS (output in `composeApp/build/compose/jars`)

`./gradlew packageReleaseDeb` package Linux deb file (output in `composeApp/build/compose/binaries`)

`./gradlew packageReleaseMsi` package Windows msi installer (output in `composeApp/build/compose/binaries`)

### Depends on libvips and its dependencies for image decoding

Check vips/native directory for cmake build details

### libvips build dependencies

- make
- cmake
- ninja
- meson
- nasm
- autotools

run `./gradlew :vips:linuxBuild` to launch linux build using system toolchain

alternatively you can run build inside docker
container 
- `cd ./vips/native`
- `docker build -t komelia-vips-build . `
- `docker run -v .:/build komelia-vips-build`

after build run `./gradlew :vips:linuxCopyVipsLibsToClasspath` to copy built libraries to be bundled with the
application

### windows cross compilation

run `./gradlew :vips:windowsBuild` to launch windows build using system toolchain (requires `mingw-w64-gcc` and `mingw-w64-binutils`)

alternatively you can run build inside docker container 
- `cd ./vips/native`
- `docker build -t komelia-vips-w64-build . -f w64.Dockerfile`
- `docker run -v .:/build komelia-vips-w64-build`

after build `./gradlew :vips:windowsCopyVipsLibsToClasspath` to copy libraries to be bundled
with the app

## Android Build

debug apk build:`./gradlew :composeApp:assemble` (output in `composeApp/build/outputs/apk/debug`)

unsigned release apk build:`./gradlew :composeApp:assembleRelease` (output in `composeApp/build/outputs/apk/release`)
