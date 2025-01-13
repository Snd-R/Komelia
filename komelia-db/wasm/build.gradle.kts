@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.snd_r"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    wasmJs {
        moduleName = "komeliaImageWorker"
        browser()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":komelia-core"))
            implementation(project(":komelia-db:shared"))
            api(project(":third_party:indexeddb:core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
        }
    }
}