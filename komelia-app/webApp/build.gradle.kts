import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd-r.komelia"
version = libs.versions.app.version.get()


kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-app"
        browser {
            commonWebpackConfig {
                outputFileName = "komelia-app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static("../../../../komelia-infra/image-decoder/wasm-image-worker/build/dist/wasmJs/productionExecutable")
                }
            }
        }

        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.komeliaApp.shared)
            implementation(projects.komeliaUi)
            implementation(projects.komeliaDomain.core)
            implementation(projects.komeliaDomain.offline)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.database.wasm)
            implementation(projects.komeliaInfra.database.transaction)
            implementation(projects.komeliaInfra.webview)
            implementation(projects.komeliaInfra.imageDecoder.wasmImageWorker)
            implementation(projects.komeliaInfra.database.wasm)

            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.browser)
            implementation(libs.indexedd)
            implementation(libs.filekit.core)
        }

    }
}