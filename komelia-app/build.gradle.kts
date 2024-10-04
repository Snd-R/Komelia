@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.protobuf)
}

group = "io.github.snd-r"
version = "0.9.0"

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

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "komelia-app"
        browser {
            commonWebpackConfig {
                outputFileName = "komelia-app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.projectDir.path + "/commonMain/")
                        add(project.projectDir.path + "/wasmJsMain/")
                        add(project.parent!!.projectDir.path + "/build/js/node_modules/wasm-vips/lib/")
                        add(project.parent!!.projectDir.path + "/wasm-image-worker/build/dist/wasmJs/productionExecutable/")
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":komelia-core"))
            implementation(project(":komelia-db:shared"))

            implementation(compose.runtime)
            implementation(compose.foundation)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor3)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.komf.client)
            implementation(libs.komga.client)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {}
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.common)

            implementation(libs.directories)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.logback.core)
            implementation(libs.logback.classic)
            implementation(libs.okhttp)
            implementation(libs.okhttp.logging.interceptor)
            implementation(libs.slf4j.api)
            implementation(project(":image-decoder"))
            implementation(files("${projectDir.parent}/third_party/jbr-api/jbr-api-1.0.2.jar"))
            implementation(project(":komelia-db:sqlite"))
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(project(":wasm-image-worker"))
            implementation(project(":komelia-db:wasm"))
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    sourceSets["main"].proto {
        srcDir("src/androidMain/proto")
    }

    defaultConfig {
        applicationId = "io.github.snd_r.komelia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.9.0"
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
            packageVersion = "0.9.0"
            description = "Komga media client"
            vendor = "Snd-R"
            appResourcesRootDir.set(
                project.parent!!.projectDir.resolve("image-decoder/desktopComposeResources")
            )

            windows {
                menu = true
                upgradeUuid = "40E86376-4E7C-41BF-8E3B-754065032B22"
                iconFile.set(project.file("src/desktopMain/resources/ic_launcher.ico"))
            }

            linux {
                iconFile.set(project.file("src/desktopMain/resources/ic_launcher.png"))
                modules("jdk.security.auth")
            }
        }

        buildTypes.release.proguard {
            version.set("7.5.0")
            optimize.set(false)
            configurationFiles.from(project.file("no_icons.pro"))
        }
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
