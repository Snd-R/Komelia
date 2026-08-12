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
        browser {
            commonWebpackConfig { devtool = "false" }
        }
    }

    sourceSets {
        wasmJsMain {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }
        wasmJsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)

            implementation(libs.kotlinx.browser)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.js)
            implementation(libs.komf.client)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.database.wasm)
            implementation(projects.komeliaUi)
        }
    }
}
