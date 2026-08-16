# Komelia - Komga media client

### Downloads:

- Latest prebuilt release is available at https://github.com/Snd-R/Komelia/releases
- Google Play Store https://play.google.com/store/apps/details?id=io.github.snd_r.komelia
- F-Droid https://f-droid.org/packages/io.github.snd_r.komelia/
- AUR package https://aur.archlinux.org/packages/komelia

## Screenshots

<details>
  <summary>Mobile</summary>
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="Komelia" width="270">  
</details>

<details>
  <summary>Tablet</summary>
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/1.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/2.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/3.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/4.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/5.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/6.jpg" alt="Komelia" width="400" height="640">  
</details>

<details>
  <summary>Desktop</summary>
   <img src="/screenshots/1.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/2.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/3.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/4.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/5.jpg" alt="Komelia" width="1280">  
</details>

## Build instructions
Make sure you download all git submodules `git clone --recurse-submodules https://github.com/Snd-R/Komelia` \
if you already cloned repository without recurse command run`git submodule update --init --recursive`

Requires jdk 17 or higher\
Android and JVM targets require C and C++ compiler for native libraries as well as Node.js for epub readers build.\
Recommended way to build native libraries is by using docker images that contain all required build dependencies.\
If you want to build with system toolchain and dependencies try running:\
`./gradlew komeliaBuildNonJvmDependencies` (Linux Only)

## Desktop App
Replace <*platform*> placeholder with your target platform. \
Available platforms include: `linux-x86_64`, `windows-x86_64`

- `docker build -t komelia-build-<platfrom> . -f ./cmake/<paltform>.Dockerfile ` \
- `docker run -v .:/build komelia-build-<paltform>`
- `./gradlew <platform>_copyJniLibs` - copy built shared libraries to resource directory that will be bundled with the
  app
- `./gradlew buildEpubReaders` - build and copy epub readers (Node.js is required for build)

Then choose your packaging option:
- `./gradlew :desktopRun` to launch desktop app
- `./gradlew :desktopJar` package jar file (output in `komelia-app/desktopApp/build/compose/jars`)
- `./gradlew :desktopDeb` package Linux deb file (output in `komelia-app/desktopApp/build/compose/binaries`)
- `./gradlew :desktopMsi` package Windows msi installer (output in `komelia-app/desktopApp/build/compose/binaries`)

## Android App
Replace <*arch*> placeholder with your target architecture.\
Available architectures include:  `aarch64`, `armv7a`, `x86_64`, `x86`

- `docker build -t komelia-build-android . -f ./cmake/android.Dockerfile `
- `docker run -v .:/build komelia-build-android <arch>`
- `./gradlew <arch>_copyJniLibs` - copy built shared libraries to resource directory that will be bundled with the app
- `./gradlew buildEpubReaders` - build and copy epub readers (Node.js is required for build)

Then choose app build option:

- `./gradlew :androidDebug` debug apk (output in `komelia-app/androidApp/build/outputs/apk/debug`)
- `./gradlew :androidRelease` unsigned release apk (output in`komelia-app/androidApp/build/outputs/apk/release`)


## Komf Wasm WebUI
run `./gradlew :komfWebUI` output will be in `./build/komf-webui`

## Komf Wasm Extension
for chrome`./gradlew :komfExtensionChrome` \
for firefox`./gradlew :komfExtensionFirefox` \
output archive will be in `./komelia-komf-extension/app/build/distributions`