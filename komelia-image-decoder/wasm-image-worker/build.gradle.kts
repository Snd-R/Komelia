import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.snd_r"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komeliaImageWorker"
        browser {
            commonWebpackConfig {
                outputFileName = "komeliaImageWorker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            api(project(":komelia-image-decoder:shared"))
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
            implementation(npm("wasm-vips", "0.0.11"))
            implementation(npm("string-replace-loader", "3.1.0"))
        }
    }
}