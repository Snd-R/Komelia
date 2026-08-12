@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    wasmJs {
        outputModuleName = "background"
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
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.komeliaKomfExtension.shared)
        }
    }
}

