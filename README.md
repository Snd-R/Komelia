# Komelia - Komga media client

### latest version available at https://github.com/Snd-R/Komelia/releases

![screenshots](./screenshots/screenshot.jpg)

## Requires libvips and its dependencies for image decoding

The recommended way to build is by using docker images that contain all required build dependencies\
If you want to build with system toolchain and dependencies go to `./image-decoder/native` and
run `build-(target-paltform).sh` script for your target paltform

To build with docker container, replace <*platform*> placeholder with your target platform\
Available platforms include: `linux-x86_64`, `windows-x86_64`, `android-arm64`, `android-x86_64`

- `cd ./image-decoder/native`
- `docker build -t komelia-build-<platfrom> . -f <paltform>.Dockerfile `
- `docker run -v .:/build komelia-build-<paltform>`
- `cd ../../`
- `./gradlew :image-decoder:<platform>_copyJniLibs` - copy built shared libraries to resource directory that will be
  bundled with the app

# Desktop App Build

Requires jdk 17 or higher

- `./gradlew run` to launch desktop app
- `./gradlew repackageUberJar` package jar for current OS (output in `composeApp/build/compose/jars`)
- `./gradlew packageReleaseDeb` package Linux deb file (output in `composeApp/build/compose/binaries`)
- `./gradlew packageReleaseMsi` (can only be run under Windows) package Windows msi installer (output
  in `composeApp/build/compose/binaries`)

# Android App Build

- debug apk build:`./gradlew :composeApp:assemble` (output in `composeApp/build/outputs/apk/debug`)
- unsigned release apk build:`./gradlew :composeApp:assembleRelease` (output in `composeApp/build/outputs/apk/release`)