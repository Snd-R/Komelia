# Komelia - Komga media client

### Downloads:
- Latest prebuilt release is available at https://github.com/Snd-R/Komelia/releases
- F-Droid https://f-droid.org/packages/io.github.snd_r.komelia/
- AUR package https://aur.archlinux.org/packages/komelia

![screenshots](./screenshots/screenshot.jpg)

# Build instructions
The recommended way to build native libraries is by using docker images that contain all required build dependencies\
If you want to build with system toolchain and dependencies try running:\
`./gradlew komeliaBuildNonJvmDependencies`

To build with docker container, replace <*platform*> placeholder with your target platform\
Available platforms include: `linux-x86_64`, `windows-x86_64`, `android-arm64`, `android-x86_64`

- `docker build -t komelia-build-<platfrom> . -f ./cmake/<paltform>.Dockerfile `
- `docker run -v .:/build komelia-build-<paltform>`
- `./gradlew <platform>_copyJniLibs` - copy built shared libraries to resource directory that will be
  bundled with the app
- `./gradlew buildWebui` - build and copy epub reader webui (npm is required for build)

## Desktop App Build

Requires jdk 17 or higher

- `./gradlew :komelia-app:run` to launch desktop app
- `./gradlew :komelia-app:repackageUberJar` package jar for current OS (output in `komelia-app/build/compose/jars`)
- `./gradlew :komelia-app:packageReleaseDeb` package Linux deb file (output in `komelia-app/build/compose/binaries`)
- `./gradlew :komelia-app:packageReleaseMsi` package Windows msi installer (output in `komelia-app/build/compose/binaries`)

## Android App Build

- debug apk build:`./gradlew :komelia-app:assemble` (output in `komelia-app/build/outputs/apk/debug`)
- unsigned release apk build:`./gradlew :komelia-app:assembleRelease` (output in `komelia-app/build/outputs/apk/release`)