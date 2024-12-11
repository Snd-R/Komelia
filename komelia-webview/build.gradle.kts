import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd-r"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komelia-webview"
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ksoup)
        }

        androidMain.dependencies {
//            implementation(libs.androidx.webkit)

        }

        jvmMain.dependencies {
            implementation(compose.desktop.common)
            api(project(":komelia-jni"))
        }
    }
}
android {
    namespace = "io.github.snd_r.webview"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
