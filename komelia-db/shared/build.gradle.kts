import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.snd-r"
version = "unspecified"

kotlin {
    jvmToolchain(17)
    jvm {}
    androidTarget {}

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komelia-db-shared"
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":komelia-core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ktor.client.core)
            implementation(libs.komga.client)

        }
    }
}

android {
    namespace = "io.github.snd_r.db.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

