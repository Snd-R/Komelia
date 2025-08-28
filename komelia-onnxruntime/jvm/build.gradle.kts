@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.snd_r.komelia.onnxruntime"
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
            api(projects.komeliaJni)
            api(projects.komeliaImageDecoder.vips)
            api(projects.komeliaOnnxruntime.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.logging)
        }

        jvmMain.dependencies {
            implementation(libs.directories)
        }
    }
}
android {
    namespace = "io.github.snd_r.komelia.onnxruntime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
