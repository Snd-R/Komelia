@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
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
            api(project(":komelia-jni"))
            api(project(":komelia-image-decoder:shared"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {}

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

            val version = "0.8.18"

            implementation(libs.slf4j.api)
            implementation(libs.directories)
            compileOnly("org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:$version")
        }
    }
}
android {
    namespace = "io.github.snd_r.image_decoder"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
