@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd_r"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            implementation(compose.components.resources)
            api(libs.kotlinx.datetime)
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
            api(libs.exposed.migration)
            api(libs.exposed.kotlin.datetime)
            implementation(libs.flyway.core)
            implementation(libs.sqlite.xerial.jdbc)
            implementation(project(":komelia-db:shared"))
            implementation(project(":komelia-core"))
        }
    }
}

android {
    namespace = "io.github.snd_r.db.sqlite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
