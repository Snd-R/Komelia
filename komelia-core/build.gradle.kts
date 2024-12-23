@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinAtomicfu)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.protobuf)
}

group = "io.github.snd-r"
version = "0.9.0"

kotlin {
    jvmToolchain(17) // max version https://developer.android.com/build/releases/gradle-plugin#compatibility
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=io.github.snd_r.komelia.platform.CommonParcelize"
            )
        }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komelia-core"
        browser()
    }

    sourceSets {
        all { languageSettings.optIn("kotlin.ExperimentalStdlibApi") }
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.materialIconsExtended)
            api(compose.material)
            api(compose.material3)
            api(compose.components.resources)

            api(libs.kotlin.logging)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.core)

            api(libs.cache4k)
            api(libs.coil)
            api(libs.coil.compose)
            api(libs.coil.network.ktor3)
            api(libs.filekit.core)
            api(libs.filekit.compose)
            api(libs.komf.client)
            api(libs.komga.client)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.encoding)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.ksoup)
            api(libs.markdown)
            api(libs.reorderable)
            api(libs.richEditor.compose)
            api(libs.voyager.screenmodel)
            api(libs.voyager.navigator)
            api(libs.voyager.transition)

            api(project(":third_party:ChipTextField:chiptextfield-m3"))
            api(project(":third_party:compose-sonner:sonner"))
            implementation(project(":komelia-webview"))
        }

        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)
            api(libs.androidx.window)
            api(libs.androidx.datastore)
            api(libs.ktor.client.okhttp)
            api(libs.logback.android)
            api(libs.okhttp)
            api(libs.okhttp.logging.interceptor)
            api(libs.protobuf.javalite)
            api(libs.protobuf.kotlin.lite)
            api(libs.slf4j.api)
            api(project(":komelia-image-decoder"))
        }

        jvmMain.dependencies {
            api(compose.desktop.common)
            api(compose.desktop.currentOs)

            api(libs.kotlinx.coroutines.swing)

            api(libs.commons.compress)
            api(libs.directories)
            api(libs.java.keyring)
            api(libs.ktor.client.okhttp)
            api(libs.logback.core)
            api(libs.logback.classic)
            api(libs.okhttp)
            api(libs.okhttp.logging.interceptor)
            api(libs.secret.service)
            api(libs.slf4j.api)
            api(project(":komelia-image-decoder"))
            api(files("${projectDir.parent}/third_party/jbr-api/jbr-api-1.0.2.jar"))
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
            api(project(":wasm-image-worker"))
        }
    }
}

android {
    namespace = "io.github.snd_r.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].proto {
        srcDir("src/androidMain/proto")
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:3.24.1"
        }
        generateProtoTasks {
            all().forEach { task ->
                task.builtins {
                    id("java") {
                        option("lite")
                    }
                    id("kotlin") {
                        option("lite")
                    }
                }
            }
        }
    }
}
