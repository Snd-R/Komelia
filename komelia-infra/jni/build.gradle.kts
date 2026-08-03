import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

group = "io.github.snd_r.komelia.infra.jni"
version = "unspecified"

kotlin {
    android {
        namespace = "io.github.snd_r.komelia.infra.jni"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {}
        androidMain.dependencies {}
        jvmMain.dependencies {

            implementation(libs.slf4j.api)
            implementation(libs.directories)
        }
    }
}
