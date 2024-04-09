import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("kotlinx-atomicfu")
}

group = "io.github.snd-r"
version = "unspecified"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

val ktorVersion = "3.0.0-beta-2-eap-928"

kotlin {
    jvmToolchain(17)
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
    }
    jvm {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komga_client"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-encoding:$ktorVersion")
            implementation("io.ktor:ktor-client-auth:$ktorVersion")
            implementation("io.ktor:ktor-sse:$ktorVersion")
            implementation("io.github.oshai:kotlin-logging:6.0.3")
        }

        val jvmMain by getting
        jvmMain.dependencies {
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
        }

        androidMain.dependencies {
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
        }
    }

}
android {
    namespace = "io.github.snd_r.komgaClient"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn", "-opt-in=kotlin.ExperimentalStdlibApi")
}
