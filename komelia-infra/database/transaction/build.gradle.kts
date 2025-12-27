import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.snd-r.komelia.db.shared"
version = "unspecified"

kotlin {
    jvmToolchain(17)
    jvm {}
    androidTarget {}

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-infra-database-transaction"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        androidMain.dependencies {
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
        }
        jvmMain.dependencies {
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia.infra.database.transaction"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

