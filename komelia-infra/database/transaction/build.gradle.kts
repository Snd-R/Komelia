import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.snd-r.komelia.db.shared"
version = "unspecified"

kotlin {
    jvm {}
    android {
        namespace = "io.github.snd_r.komelia.infra.database.transaction"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-infra-database-transaction"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        androidMain.dependencies {
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
        }
        jvmMain.dependencies {
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
        }
    }
}



