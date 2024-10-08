@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.codegen.gradle.CodegenTask

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooqCodegen)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd_r"
version = "unspecified"

val sqliteUrl = "jdbc:sqlite:${project.layout.buildDirectory.get()}/generated/flyway/database.sqlite"
val sqliteMigrationDir = "$projectDir/src/commonMain/composeResources/files"
val jooqOutputDir = "${project.layout.buildDirectory.get()}/generated/jooq"

kotlin {
    jvmToolchain(17)

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            implementation(compose.components.resources)
            api(libs.kotlinx.datetime)
            api(libs.jooq)
            implementation(libs.flyway.core)
            implementation(libs.sqlite.xerial.jdbc)
            implementation(project(":komelia-db:shared"))
            implementation(project(":komelia-core"))
        }
        commonMain { kotlin { srcDir(jooqOutputDir) } }
    }
}

android {
    namespace = "io.github.snd_r.db.sqlite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    jooqCodegen(libs.sqlite.xerial.jdbc)
    flyway(libs.sqlite.xerial.jdbc)
}

tasks.flywayMigrate {
    url = sqliteUrl
    locations = arrayOf("filesystem:$sqliteMigrationDir")
    inputs.dir(sqliteMigrationDir)
    outputs.dir("${project.layout.buildDirectory.get()}/generated/flyway")
    doFirst {
        delete(outputs.files)
        mkdir("${project.layout.buildDirectory.get()}/generated/flyway")
    }
    mixed = true
}

jooq {
    configuration {
        jdbc {
            driver = "org.sqlite.JDBC"
            url = "jdbc:sqlite:${project.layout.buildDirectory.get()}/generated/flyway/database.sqlite"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            generate {
//                isKotlinNotNullPojoAttributes = true
                isKotlinNotNullRecordAttributes = true
                isKotlinNotNullInterfaceAttributes = false
            }
            database {
                name = "org.jooq.meta.sqlite.SQLiteDatabase"
            }
            target {
                packageName = "snd.komelia.db.jooq"
                directory = jooqOutputDir
            }
        }
    }
}

tasks.named<CodegenTask>("jooqCodegen") { dependsOn("flywayMigrate") }
tasks.withType<KotlinCompile> { dependsOn("jooqCodegen") }


