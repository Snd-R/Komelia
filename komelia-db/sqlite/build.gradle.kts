@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd_r.komelia.db.sqlite"
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
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            implementation(compose.components.resources)
            api(libs.kotlinx.datetime)
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
            api(libs.exposed.json)
            api(libs.exposed.kotlin.datetime)
            implementation(libs.flyway.core)
            implementation(libs.sqlite.xerial.jdbc)
            implementation(projects.komeliaDb.shared)
            implementation(projects.komeliaImageDecoder.shared)
            implementation(projects.komeliaCore)
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia.db.sqlite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val sqliteExtract: Configuration by configurations.creating
dependencies { sqliteExtract(libs.sqlite.xerial.jdbc) }
tasks.register<Sync>("android-arm64-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/aarch64/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/arm64-v8a")
}

tasks.register<Sync>("android-armv7a-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/arm/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/armeabi-v7a")
}

tasks.register<Sync>("android-x86_64-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/x86_64/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/x86_64")
}

tasks.register<Sync>("android-x86-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/x86/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/x86")
}
