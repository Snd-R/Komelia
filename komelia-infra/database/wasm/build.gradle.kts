@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.snd_r.komelia.db.wasm"
version = "unspecified"

kotlin {
    wasmJs {
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }
        wasmJsMain.dependencies {
            implementation(projects.komeliaDomain.core)
            implementation(projects.komeliaDomain.offline)
            implementation(projects.komeliaDomain.komgaApi)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.imageDecoder.shared)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.datetime)
            implementation(libs.filekit.core)
            implementation(libs.indexeddb)
        }
    }
}