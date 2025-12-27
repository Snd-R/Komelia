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
        outputModuleName = "komelia-infra-databaseb"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
//            implementation(projects.komeliaCore)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.imageDecoder.shared)
            implementation(projects.thirdParty.indexeddb.core)
        }
    }
}