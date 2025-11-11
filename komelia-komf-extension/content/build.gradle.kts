@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    wasmJs {
        outputModuleName = "content"
        binaries.executable()
        browser()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.js)
            implementation(projects.komeliaCore)
            implementation(projects.komeliaDb.shared)
            implementation(projects.komeliaDb.wasm)
        }
    }
}

