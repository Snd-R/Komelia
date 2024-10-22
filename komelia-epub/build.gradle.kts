@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd-r"
version = "unspecified"

kotlin {
    jvmToolchain(17)

//    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            api(libs.kotlinx.serialization.core)
            api(libs.kotlinx.serialization.json)
        }

//        androidMain.dependencies {}

        jvmMain.dependencies {
            implementation(compose.desktop.common)
            api(project(":komelia-jni"))
        }
    }
}
android {
    namespace = "io.github.snd_r.webview"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


