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
        moduleName = "popup"
        binaries.executable()
        browser()
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-attach-js-exception")

        }
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":komelia-komf-extension:shared"))
        }
    }
}

