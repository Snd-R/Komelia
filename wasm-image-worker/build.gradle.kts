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
        val wasmJsMain by getting
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
            implementation(npm("wasm-vips", "0.0.10"))
            implementation(npm("string-replace-loader", "3.1.0"))
        }
    }
}