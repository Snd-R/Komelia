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
        outputModuleName = "komeliaImageWorker"
        browser {
            commonWebpackConfig {
                outputFileName = "komeliaImageWorker.js"
            }
        }
        binaries.executable()
//        compilerOptions{
//            freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
//        }
    }

    sourceSets {
        wasmJsMain.dependencies {
            api(project(":komelia-image-decoder:shared"))
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
//            implementation(npm("wasm-vips", "0.0.11"))
        }
    }
}