@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.file.DuplicatesStrategy.EXCLUDE
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("de.undercouch.download") version "5.6.0"
}

group = "io.github.snd_r"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
        }

        val jvmMain by getting
        jvmMain.dependencies {
            val osName = System.getProperty("os.name")
            val hostOs = when {
                osName.startsWith("Win") -> "windows"
                osName.startsWith("Linux") -> "linux"
                else -> error("Unsupported OS: $osName")
            }

            val hostArch = when (val osArch = System.getProperty("os.arch")) {
                "x86_64", "amd64" -> "x64"
                "aarch64" -> "arm64"
                else -> error("Unsupported arch: $osArch")
            }

            val version = "0.8.10"

            implementation("org.slf4j:slf4j-api:2.0.13")
            implementation("dev.dirs:directories:26")
            compileOnly("org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:$version")
        }

        androidMain.dependencies {
        }
    }
}
android {
    namespace = "io.github.snd_r.image_decoder"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

val linuxBuildDir = "$projectDir/native/build"
val windowsBuildDir = "$projectDir/native/build-w64"
val resourcesDir = "$projectDir/src/jvmMain/resources/"
val androidArm64BuildDir = "$projectDir/native/build-android-arm64"
val androidx8664BuildDir = "$projectDir/native/build-android-x86_64"
val androidJniLibsDir = "$projectDir/src/androidMain/jniLibs"

val linuxCommonLibs = setOf(
    "libintl.so",
    "libbrotlicommon.so",
    "libbrotlidec.so",
    "libbrotlienc.so",
    "libde265.so",
    "libdav1d.so",
    "libexpat.so",
    "libffi.so",
    "libfftw3.so",
    "libgio-2.0.so",
    "libglib-2.0.so",
    "libgmodule-2.0.so",
    "libgobject-2.0.so",
    "libheif.so",
    "libhwy.so",
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
    "libkomelia_enumerate_devices_vulkan.so",
    "libkomelia_skia.so",
)

tasks.register<Sync>("linux-x86_64_copyJniLibs") {
    group = "jni"
    from("$linuxBuildDir/fakeroot/lib/")
    into(resourcesDir)
    include { it.name in desktopLinuxLibs }
}

tasks.register<Sync>("android-arm64_copyJniLibs") {
    group = "jni"

    from("$androidArm64BuildDir/fakeroot/lib/")
    into("$androidJniLibsDir/arm64-v8a/")
    include { it.name in androidLibs }
}

tasks.register<Sync>("android-x86_64_copyJniLibs") {
    group = "jni"
    from("$androidx8664BuildDir/fakeroot/lib/")
    into("$androidJniLibsDir/x86_64/")
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

    val libs = setOf(
        "libbrotlicommon.dll",
        "libbrotlidec.dll",
        "libbrotlienc.dll",
        "libde265.dll",
        "libdav1d.dll",
        "libexpat-1.dll",
        "libffi-8.dll",
        "libfftw3.dll",
        "libgio-2.0-0.dll",
        "libglib-2.0-0.dll",
        "libgmodule-2.0-0.dll",
        "libgobject-2.0-0.dll",
        "libheif.dll",
        "libhwy.dll",
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
    )
    duplicatesStrategy = EXCLUDE

    from("$windowsBuildDir/fakeroot/bin/")
    into(resourcesDir)
    include { it.name in libs }

    // include mingw dlls if compiled using system toolchain
    from("/usr/x86_64-w64-mingw32/bin/")
    include("libstdc++-6.dll")
    include("libwinpthread-1.dll")
    include("libgcc_s_seh-1.dll")
    include("libgomp-1.dll")
    into(resourcesDir)
}
