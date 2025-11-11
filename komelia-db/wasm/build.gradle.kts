@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.snd_r.komelia.db.wasm"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    wasmJs {
        outputModuleName = "komelia-db"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
            implementation(projects.komeliaCore)
            implementation(projects.komeliaDb.shared)
            implementation(projects.komeliaImageDecoder.shared)
            implementation(projects.thirdParty.indexeddb.core)
        }
    }
}