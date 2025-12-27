import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAtomicfu)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.protobuf)
}

group = "io.github.snd-r.komelia.domain.core"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-webview"
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
            api(projects.komeliaDomain.komgaApi)
            api(projects.komeliaDomain.offline)
            api(projects.komeliaInfra.database.transaction)
            api(projects.komeliaInfra.imageDecoder.shared)
            api(projects.komeliaInfra.onnxruntime.api)

            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            implementation(libs.cache4k)
            api(libs.coil)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.filekit.core)
            api(libs.komf.client)
            api(libs.komga.client)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.encoding)
            api(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ksoup)
            implementation(libs.markdown)
            implementation(libs.reorderable)
            implementation(libs.richEditor.compose)
        }

        androidMain.dependencies {
            api(libs.androidx.datastore)
            implementation(libs.androidx.appcompat)
            implementation(libs.commons.compress)
            api(libs.ktor.client.okhttp)
            api(libs.logback.android)
            api(libs.okhttp)
            api(libs.okhttp.logging.interceptor)
            implementation(libs.protobuf.javalite)
            implementation(libs.protobuf.kotlin.lite)
            implementation(libs.slf4j.api)
            implementation(projects.komeliaInfra.imageDecoder.vips)
            implementation(projects.komeliaInfra.onnxruntime.jvm)
        }

        jvmMain.dependencies {
            implementation(libs.commons.compress)
            api(libs.directories)
            implementation(libs.java.keyring)
            implementation(libs.jbr.api)
            api(libs.ktor.client.okhttp)
            api(libs.logback.core)
            api(libs.logback.classic)
            api(libs.okhttp)
            api(libs.okhttp.logging.interceptor)
            implementation(libs.secret.service)
            implementation(libs.slf4j.api)
            implementation(projects.komeliaInfra.imageDecoder.vips)
            implementation(projects.komeliaInfra.onnxruntime.jvm)
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia.domain.core"
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
