@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.snd_r"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    wasmJs {
        moduleName = "komeliaImageWorker"
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":komelia-core"))
            implementation(project(":komelia-db:shared"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}