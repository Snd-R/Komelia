import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAtomicfu) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.parcelize) apply false
}

// https://youtrack.jetbrains.com/issue/CMP-5831
allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" && requested.name == "atomicfu") {
                useVersion(libs.versions.kotlinx.atomicfu.get())
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.11.1"
    distributionType = Wrapper.DistributionType.ALL
}

val linuxBuildDir = "$projectDir/cmake/build"
val windowsBuildDir = "$projectDir/cmake/build-w64"
val androidArm64BuildDir = "$projectDir/cmake/build-android-arm64"
val androidArmv7aBuildDir = "$projectDir/cmake/build-android-armv7a"
val androidx8664BuildDir = "$projectDir/cmake/build-android-x86_64"
val androidx86BuildDir = "$projectDir/cmake/build-android-x86"

val resourcesDir = "$projectDir/komelia-jni/src/jvmMain/resources/"
val androidJniLibsDir = "$projectDir/komelia-jni/src/androidMain/jniLibs"
val composeDistroResourcesDir = "$projectDir/komelia-app/desktopUnpackedResources"
val composeCommonResources = "$projectDir/komelia-core/src/commonMain/composeResources/files"

val linuxCommonLibs = setOf(
    "libintl.so",
    "libbrotlicommon.so",
    "libbrotlidec.so",
    "libbrotlienc.so",
    "libde265.so",
    "libdav1d.so",
    "libexif.so",
    "libexpat.so",
    "libffi.so",
    "libgio-2.0.so",
    "libglib-2.0.so",
    "libgmodule-2.0.so",
    "libgobject-2.0.so",
    "libheif.so",
    "libhwy.so",
    "liblcms2.so",
    "libjpeg.so",
    "libjxl.so",
    "libjxl_cms.so",
    "libjxl_threads.so",
    "libsharpyuv.so",
    "libspng.so",
    "libtiff.so",
    "libturbojpeg.so",
    "libvips.so",
    "libwebp.so",
    "libwebpdecoder.so",
    "libwebpdemux.so",
    "libwebpmux.so",
    "libz.so",
    "libkomelia_vips.so",
)
val androidLibs = linuxCommonLibs + setOf("libkomelia_android_bitmap.so", "libiconv.so", "libomp.so")
val desktopLinuxLibs = linuxCommonLibs + setOf(
    "libkomelia_onnxruntime.so",
    "libkomelia_enumerate_devices_cuda.so",
    "libkomelia_enumerate_devices_rocm.so",
    "libkomelia_skia.so",
    "libkomelia_webview.so",
    "libkomelia_webkit_extension.so",
)
val desktopJniLibs = setOf(
    "libkomelia_vips.so",
    "libkomelia_onnxruntime.so",
    "libkomelia_enumerate_devices_cuda.so",
    "libkomelia_enumerate_devices_rocm.so",
    "libkomelia_skia.so",
    "libkomelia_webview.so",
    "libkomelia_webkit_extension.so",
)

val windowsLibs = setOf(
    "libbrotlicommon.dll",
    "libbrotlidec.dll",
    "libbrotlienc.dll",
    "libde265.dll",
    "libdav1d.dll",
    "libexif-12.dll",
    "libexpat-1.dll",
    "libffi-8.dll",
    "libgio-2.0-0.dll",
    "libglib-2.0-0.dll",
    "libgmodule-2.0-0.dll",
    "libgobject-2.0-0.dll",
    "libheif.dll",
    "libhwy.dll",
    "liblcms2-2.dll",
    "libintl-8.dll",
    "libjpeg-62.dll",
    "libjxl.dll",
    "libjxl_cms.dll",
    "libjxl_threads.dll",
    "libsharpyuv.dll",
    "libspng.dll",
    "libtiff.dll",
    "libvips-42.dll",
    "libwebp.dll",
    "libwebpdecoder.dll",
    "libwebpdemux.dll",
    "libwebpmux.dll",
    "libz1.dll",
    "libstdc++-6.dll",
    "libwinpthread-1.dll",
    "libgcc_s_seh-1.dll",
    "libgomp-1.dll",
    "libkomelia_vips.dll",
    "libkomelia_onnxruntime.dll",
    "libkomelia_onnxruntime_dml.dll",
    "libkomelia_enumerate_devices_dxgi.dll",
    "libkomelia_enumerate_devices_cuda.dll",
    "libkomelia_webview.dll",
)

tasks.register<Sync>("linux-x86_64_copyJniLibs") {
    group = "jni"
    from("$linuxBuildDir/sysroot/lib/")
    into(resourcesDir)
    include { it.name in desktopLinuxLibs }
}

tasks.register<Sync>("android-arm64_copyJniLibs") {
    group = "jni"
    dependsOn(":komelia-db:sqlite:android-arm64-ExtractSqliteLib")

    from("$androidArm64BuildDir/sysroot/lib/")
    into("$androidJniLibsDir/arm64-v8a/")
    include { it.name in androidLibs }
}

tasks.register<Sync>("android-armv7a_copyJniLibs") {
    group = "jni"
    dependsOn(":komelia-db:sqlite:android-armv7a-ExtractSqliteLib")

    from("$androidArmv7aBuildDir/sysroot/lib/")
    into("$androidJniLibsDir/armeabi-v7a/")
    include { it.name in androidLibs }
}

tasks.register<Sync>("android-x86_64_copyJniLibs") {
    group = "jni"
    dependsOn(":komelia-db:sqlite:android-x86_64-ExtractSqliteLib")
    from("$androidx8664BuildDir/sysroot/lib/")
    into("$androidJniLibsDir/x86_64/")
    include { it.name in androidLibs }
}

tasks.register<Sync>("android-x86_copyJniLibs") {
    group = "jni"
    dependsOn(":komelia-db:sqlite:android-x86-ExtractSqliteLib")
    from("$androidx86BuildDir/sysroot/lib/")
    into("$androidJniLibsDir/x86/")
    include { it.name in androidLibs }
}

tasks.register<Delete>("cleanJni") {
    group = "jni"
    delete(linuxBuildDir)
    delete(windowsBuildDir)
    delete(fileTree(resourcesDir))
}

tasks.register<Sync>("windows-x86_64_copyJniLibs") {
    group = "jni"

    duplicatesStrategy = EXCLUDE
    from("$windowsBuildDir/sysroot/bin/")
    into(resourcesDir)
    include { it.name in windowsLibs }

    // include mingw dlls if compiled using system toolchain
    from("/usr/x86_64-w64-mingw32/bin/")
    include("libstdc++-6.dll")
    include("libwinpthread-1.dll")
    include("libgcc_s_seh-1.dll")
    include("libgomp-1.dll")
    into(resourcesDir)
}

tasks.register<Sync>("windows-x86_64_copyJniLibsComposeResources") {
    group = "jni"

    duplicatesStrategy = EXCLUDE
    from("$windowsBuildDir/sysroot/bin/")
    into("$composeDistroResourcesDir/windows")
    include { it.name in windowsLibs }

    // include mingw dlls if compiled using system toolchain
    from("/usr/x86_64-w64-mingw32/bin/")
    include("libstdc++-6.dll")
    include("libwinpthread-1.dll")
    include("libgcc_s_seh-1.dll")
    include("libgomp-1.dll")
    into("$composeDistroResourcesDir/windows")
}


val webui = "$rootDir/epub-reader-webui"
val webuiKomga = "$webui/komga-webui"
val webuiTtsu = "$webui/ttu-ebook-reader"
tasks.register<Exec>("komgaNpmInstall") {
    group = "web"
    workingDir(webuiKomga)
    inputs.file("$webuiKomga/package.json")
    outputs.dir("$webuiKomga/node_modules")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "npm.cmd"
        } else {
            "npm"
        },
        "install",
    )
}

tasks.register<Exec>("komgaNpmBuild") {
    group = "web"
    dependsOn("komgaNpmInstall")
    workingDir(webuiKomga)
    inputs.dir(webuiKomga)
    outputs.dir("$webuiKomga/dist")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "npm.cmd"
        } else {
            "npm"
        },
        "run",
        "build",
    )
}

tasks.register<Exec>("ttsuNpmInstall") {
    group = "web"
    workingDir(webuiTtsu)
    inputs.file("$webuiTtsu/package.json")
    outputs.dir("$webuiTtsu/node_modules")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "npm.cmd"
        } else {
            "npm"
        },
        "install",
    )
}

tasks.register<Exec>("ttsuNpmBuild") {
    group = "web"
    dependsOn("ttsuNpmInstall")
    workingDir(webuiTtsu)
    inputs.dir(webuiTtsu)
    outputs.dir("$webuiTtsu/dist")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "npm.cmd"
        } else {
            "npm"
        },
        "run",
        "build",
    )
}

tasks.register<Sync>("buildWebui") {
    group = "web"
    dependsOn("komgaNpmBuild")
    dependsOn("ttsuNpmBuild")

    from("$webuiKomga/dist/")
    from("$webuiTtsu/dist/")
    into(composeCommonResources)
}

tasks.register<Exec>("cmakeSystemDepsConfigure") {
    group = "jni"
    delete("$projectDir/cmake-build")
    inputs.file("$projectDir/komelia-image-decoder/vips/native/CMakeLists.txt")
    inputs.file("$projectDir/komelia-webview/native/CMakeLists.txt")
    commandLine(
        "cmake",
        "-B", "cmake-build",
        "-G", "Ninja",
        "-DCMAKE_BUILD_TYPE=Release",
        "-DKOMELIA_SUPERBUILD=OFF"
    )
}

tasks.register<Exec>("cmakeSystemDepsBuild") {
    group = "jni"
    dependsOn("cmakeSystemDepsConfigure")
    inputs.dir("$projectDir/cmake-build")
    outputs.dir("$projectDir/cmake-build/komelia-image-decoder/native")
    outputs.dir("$projectDir/cmake-build/komelia-webview/native")
    commandLine(
        "cmake",
        "--build",
        "cmake-build",
        "--parallel"
    )
}

tasks.register<Sync>("cmakeSystemDepsCopyJniLibs") {
    group = "jni"
    dependsOn("cmakeSystemDepsBuild")
    inputs.dir("$projectDir/cmake-build/komelia-webview/native")
    inputs.dir("$projectDir/cmake-build/komelia-image-decoder/vips/native")
    outputs.dir(resourcesDir)

    from(
        "$projectDir/cmake-build/komelia-image-decoder/vips/native",
        "$projectDir/cmake-build/komelia-webview/native"
    )
    into(resourcesDir)
    include { it.name in desktopJniLibs }
}

tasks.register("komeliaBuildNonJvmDependencies") {
    group = "build"
    dependsOn("buildWebui")
    dependsOn("cmakeSystemDepsCopyJniLibs")
}