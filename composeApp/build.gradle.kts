@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.protobuf") version "0.9.4"
}

group = "io.github.snd-r"
version = "0.6.0"

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

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.projectDir.path + "/commonMain/")
                        add(project.projectDir.path + "/wasmJsMain/")
                        add(project.parent!!.projectDir.path + "/build/js/node_modules/wasm-vips/lib/")
                        add(project.parent!!.projectDir.path + "/wasmImageWorker/build/dist/wasmJs/productionExecutable/")
                    }
                }
            }
        }
        binaries.executable()
    }

    val coilVersion = "3.0.0-alpha08"
    val ktorVersion = "3.0.0-beta-2-eap-972"
    val voyagerVersion = "1.1.0-beta02"
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material)
            implementation(compose.material3)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.1")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            implementation("io.github.oshai:kotlin-logging:7.0.0")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-encoding:$ktorVersion")

            implementation("io.coil-kt.coil3:coil:$coilVersion")
            implementation("io.coil-kt.coil3:coil-compose:$coilVersion")
            implementation("io.coil-kt.coil3:coil-network-ktor:$coilVersion")

            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            implementation("cafe.adriel.lyricist:lyricist:1.7.0")

            implementation("io.github.dokar3:sonner:0.3.8")
            implementation("io.github.dokar3:chiptextfield-m3:0.7.0-alpha05")

            implementation("sh.calvin.reorderable:reorderable:2.1.1")

            implementation("com.darkrockstudios:mpfilepicker")
            implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")

            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc05")
            implementation("org.jetbrains:markdown:0.7.3")

            implementation(project(":komga_client"))
        }

        androidMain.dependencies {
            api("androidx.activity:activity-compose:1.9.0")
            api("androidx.appcompat:appcompat:1.7.0")
            api("androidx.core:core-ktx:1.13.1")
            implementation("androidx.window:window:1.3.0")
            implementation("androidx.datastore:datastore:1.1.1")

            implementation("org.slf4j:slf4j-api:2.0.13")
            implementation("com.github.tony19:logback-android:3.0.0")

            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

            implementation("com.google.protobuf:protobuf-javalite:3.21.11")
            implementation("com.google.protobuf:protobuf-kotlin-lite:3.21.11")

            implementation(project(":image-decoder"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)

            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")

            implementation("org.slf4j:slf4j-api:2.0.13")
            implementation("ch.qos.logback:logback-core:1.5.6")
            implementation("ch.qos.logback:logback-classic:1.5.6")

            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

            implementation("com.akuleshov7:ktoml-core:0.5.2")
            implementation("com.akuleshov7:ktoml-file:0.5.2")
            implementation("com.akuleshov7:ktoml-source-jvm:0.5.2")

            implementation("dev.dirs:directories:26")
            implementation("com.github.javakeyring:java-keyring:1.0.4")
            implementation("de.swiesend:secret-service:2.0.1-alpha")
            implementation("org.apache.commons:commons-compress:1.26.2")

//            implementation("com.twelvemonkeys.imageio:imageio-core:3.10.1")
//            implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.10.1")
//            implementation("com.twelvemonkeys.imageio:imageio-webp:3.10.1")
            implementation(project(":image-decoder"))

            implementation(files("jbr-api/jbr-api-6.4.2.jar"))
        }

        val wasmJsMain by getting
        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:$ktorVersion")
            implementation(project(":wasmImageWorker"))
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    sourceSets["main"].proto {
        srcDir("src/androidMain/proto")
    }

    defaultConfig {
        applicationId = "io.github.snd_r.komelia"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
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


compose.desktop {
    application {
        mainClass = "io.github.snd_r.komelia.MainKt"

        jvmArgs += listOf(
            "-Dkotlinx.coroutines.scheduler.max.pool.size=3",
            "-Dkotlinx.coroutines.scheduler.core.pool.size=3",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UseShenandoahGC",
            "-XX:ShenandoahGCHeuristics=compact",
            "-XX:ConcGCThreads=1",
            "-XX:TrimNativeHeapInterval=60000",
        )

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Komelia"
            packageVersion = "0.6.0"
            description = "Komga media client"
            vendor = "Snd-R"
            modules("jdk.unsupported", "jdk.security.auth")

            windows {
                menu = true
                upgradeUuid = "40E86376-4E7C-41BF-8E3B-754065032B22"
                iconFile.set(project.file("src/desktopMain/resources/ic_launcher.ico"))
            }

            linux {
                iconFile.set(project.file("src/desktopMain/resources/ic_launcher.png"))
            }
        }

        buildTypes.release.proguard {
            version.set("7.5.0")
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

tasks.register<Zip>("repackageUberJar") {
    group = "compose desktop"
    val packageUberJarForCurrentOS = tasks.getByName("packageReleaseUberJarForCurrentOS")
    dependsOn(packageUberJarForCurrentOS)
    val file = packageUberJarForCurrentOS.outputs.files.first()
    val output = File(file.parentFile, "${file.nameWithoutExtension}-repacked.jar")
    archiveFileName.set(output.absolutePath)
    destinationDirectory.set(file.parentFile.absoluteFile)

    from(project.zipTree(file)) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.RSA")
        exclude("META-INF/*.DSA")
        exclude("META-INF/services/javax.imageio.spi.ImageReaderSpi")
    }

    // ImageIO plugins include workaround https://github.com/JetBrains/compose-multiplatform/issues/4505
    from("$projectDir/javax.imageio.spi.ImageReaderSpi") {
        into("META-INF/services")
    }

    doLast {
        delete(file)
        output.renameTo(file)
        logger.lifecycle("The repackaged jar is written to ${archiveFile.get().asFile.canonicalPath}")
    }
}
