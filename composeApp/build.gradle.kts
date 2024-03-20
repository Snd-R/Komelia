import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")

    id("com.google.devtools.ksp").version("1.9.22-1.0.17")
    id("dev.hydraulic.conveyor") version "1.9"
}

group = "io.github.snd-r"
version = "0.1"

dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")
    jvmToolchain(21)

    val coilVersion = "3.0.0-alpha06"
    val ktorVersion = "3.0.0-beta-1"
    val voyagerVersion = "1.0.0"
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }
        val desktopMain by getting

        commonMain.dependencies {

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")

            implementation("org.slf4j:slf4j-api:2.0.12")
            implementation("io.github.oshai:kotlin-logging:6.0.3")

            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")

            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


            implementation("io.coil-kt.coil3:coil:$coilVersion")
            implementation("io.coil-kt.coil3:coil-compose:$coilVersion")
            implementation("io.coil-kt.coil3:coil-network-ktor:$coilVersion")

            implementation("cafe.adriel.voyager:voyager-screenmodel-desktop:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-navigator-desktop:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-tab-navigator-desktop:$voyagerVersion")

            implementation("io.github.dokar3:sonner:0.3.1")
            implementation("io.github.dokar3:chiptextfield-m3:0.7.0-alpha01")

            implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
            implementation("org.apache.commons:commons-lang3:3.14.0")
            implementation("org.jsoup:jsoup:1.17.2")

            implementation(project(":komga_client"))
        }

        androidMain.dependencies {
            api("androidx.activity:activity-compose:1.8.2")
            api("androidx.appcompat:appcompat:1.6.1")
            api("androidx.core:core-ktx:1.12.0")

            implementation("com.github.tony19:logback-android:3.0.0")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
            implementation("androidx.window:window:1.2.0")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)

            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

            implementation("ch.qos.logback:logback-core:1.5.3")
            implementation("ch.qos.logback:logback-classic:1.5.3")

            implementation("com.akuleshov7:ktoml-core:0.5.0")
            implementation("com.akuleshov7:ktoml-file:0.5.0")
            implementation("com.akuleshov7:ktoml-source-jvm:0.5.0")

            implementation("dev.dirs:directories:26")
            implementation("com.github.javakeyring:java-keyring:1.0.4")
            implementation(project(":vips"))

//            implementation("com.twelvemonkeys.imageio:imageio-core:3.10.1")
//            runtimeOnly("com.twelvemonkeys.imageio:imageio-jpeg:3.10.1")
//            runtimeOnly("com.twelvemonkeys.imageio:imageio-webp:3.10.1")
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "io.github.snd_r.komelia"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation("libs.compose.ui.tooling:1.6.2")
    }
}

compose.desktop {
    application {
        mainClass = "io.github.snd_r.komelia.MainKt"

        buildTypes.release.proguard {
            version.set("7.4.2")
            optimize.set(false)
            configurationFiles.from(project.file("no_icons.pro"))
        }
    }
}

configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn", "-opt-in=kotlin.ExperimentalStdlibApi")
}


tasks.register<Zip>("repackageUberJar") {
    val packageUberJarForCurrentOS = tasks.getByName("packageUberJarForCurrentOS")
    dependsOn(packageUberJarForCurrentOS)
    val file = packageUberJarForCurrentOS.outputs.files.first()
    val output = File(file.parentFile, "${file.nameWithoutExtension}-repacked.jar")
    archiveFileName.set(output.absolutePath)
    destinationDirectory.set(file.parentFile.absoluteFile)
    exclude("META-INF/*.SF")
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.DSA")
    from(project.zipTree(file))
    doLast {
        delete(file)
        output.renameTo(file)
        logger.lifecycle("The repackaged jar is written to ${archiveFile.get().asFile.canonicalPath}")
    }
}
