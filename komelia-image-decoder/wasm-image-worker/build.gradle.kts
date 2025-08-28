import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.snd_r.komelia.image_decoder"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komeliaImageWorker"
        browser {
            commonWebpackConfig {
                outputFileName = "komeliaImageWorker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
            api(projects.komeliaImageDecoder.shared)
//            implementation(npm("wasm-vips", "0.0.11"))
        }
    }
}