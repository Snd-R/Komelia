import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

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
        moduleName = "imageWorker"
        browser {
            commonWebpackConfig {
                outputFileName = "imageWorker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            implementation(npm("wasm-vips", "0.0.8"))
            implementation(npm("string-replace-loader", "3.1.0"))
        }
    }
}