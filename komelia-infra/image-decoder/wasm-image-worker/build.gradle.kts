import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.snd_r.komelia.infra.image_decoder"
version = "unspecified"

kotlin {

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
            api(projects.komeliaInfra.imageDecoder.shared)
//            implementation(npm("wasm-vips", "0.0.11"))
        }
    }
}