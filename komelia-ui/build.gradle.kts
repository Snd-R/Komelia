@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAtomicfu)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
}

group = "io.github.snd-r.komelia.ui"
version = "0.9.0"

kotlin {
    jvmToolchain(17) // max version https://developer.android.com/build/releases/gradle-plugin#compatibility
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=snd.komelia.ui.platform.CommonParcelize",
            )
        }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-core"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            api(projects.komeliaDomain.core)
            api(projects.komeliaDomain.komgaApi)
            api(projects.komeliaDomain.offline)
            api(projects.komeliaInfra.imageDecoder.shared)
            api(projects.komeliaInfra.onnxruntime.api)
            implementation(projects.komeliaInfra.webview)
            implementation(projects.komeliaInfra.database.transaction)
            implementation(projects.thirdParty.chipTextField.chiptextfieldM3)
            implementation(projects.thirdParty.composeSonner.sonner)

            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.materialIconsExtended)
            api(libs.compose.resources)

            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            implementation(libs.cache4k)
            implementation(libs.coil)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.komf.client)
            implementation(libs.komga.client)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ksoup)
            implementation(libs.markdown)
            implementation(libs.reorderable)
            implementation(libs.richEditor.compose.get().toString()){
                exclude(group = "org.jetbrains.compose.material", module = "material")
            }
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transition)

        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.datastore)
            implementation(libs.commons.compress)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.android)
            implementation(libs.okhttp)
            implementation(libs.okhttp.logging.interceptor)
            implementation(libs.slf4j.api)
            implementation(projects.komeliaInfra.imageDecoder.vips)
            implementation(projects.komeliaInfra.onnxruntime.jvm)
        }

        jvmMain.dependencies {
            api(compose.desktop.currentOs)
            api(libs.compose.desktop)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.commons.compress)
            implementation(libs.directories)
            implementation(libs.jbr.api)
            implementation(libs.java.keyring)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.core)
            implementation(libs.logback.classic)
            implementation(libs.okhttp)
            implementation(libs.okhttp.logging.interceptor)
            implementation(libs.secret.service)
            implementation(libs.slf4j.api)
            implementation(projects.komeliaInfra.imageDecoder.vips)
            implementation(projects.komeliaInfra.onnxruntime.jvm)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
            implementation(projects.komeliaInfra.imageDecoder.wasmImageWorker)
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
