import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.snd-r"
version = libs.versions.app.version.get()

kotlin {
    jvmToolchain(17) // max version https://developer.android.com/build/releases/gradle-plugin#compatibility
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "komelia-app"
        browser {
            commonWebpackConfig {
                outputFileName = "komelia-app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.rootDir.path)
                        add(project.parent!!.projectDir.path + "/komelia-image-decoder/wasm-image-worker/build/dist/wasmJs/productionExecutable/")
                    }
                }
            }
        }
        browser()
        binaries.executable()
//        compilerOptions{
//            freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
//        }
    }

    sourceSets {
        all { languageSettings.optIn("kotlin.ExperimentalUnsignedTypes") }
        commonMain.dependencies {
            implementation(project(":komelia-core"))
            implementation(project(":komelia-db:shared"))
            implementation(project(":komelia-webview"))
        }

        androidMain.dependencies {
            implementation(project(":komelia-db:sqlite"))
            implementation(libs.filekit.core)
        }
        jvmMain.dependencies {
            implementation(project(":komelia-db:sqlite"))
            implementation(project(":komelia-image-decoder:vips"))
            implementation(files("${projectDir.parent}/third_party/jbr-api/jbr-api-1.0.2.jar"))
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(project(":komelia-image-decoder:wasm-image-worker"))
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

    defaultConfig {
        applicationId = "io.github.snd_r.komelia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 9
        versionName = libs.versions.app.version.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,README.txt}"
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "android.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


compose.desktop {
    application {
        mainClass = "io.github.snd_r.komelia.MainKt"

        jvmArgs += listOf(
            "-Dkotlinx.coroutines.scheduler.max.pool.size=4",
            "-Dkotlinx.coroutines.scheduler.core.pool.size=4",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UseShenandoahGC",
            "-XX:ShenandoahGCHeuristics=compact",
            "-XX:ConcGCThreads=1",
            "-XX:TrimNativeHeapInterval=60000",
        )

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Komelia"
            packageVersion = libs.versions.app.version.get()
            description = "Komga media client"
            vendor = "Snd-R"
            appResourcesRootDir.set(
                project.projectDir.resolve("desktopUnpackedResources")
            )
            modules("jdk.security.auth", "java.sql")

            windows {
                menu = true
                upgradeUuid = "40E86376-4E7C-41BF-8E3B-754065032B22"
                iconFile.set(project.file("src/jvmMain/resources/ic_launcher.ico"))
            }

            linux {
                iconFile.set(project.file("src/jvmMain/resources/ic_launcher.png"))
            }
        }

        buildTypes.release.proguard {
            version.set("7.7.0")
            optimize.set(false)
            configurationFiles.from(project.file("desktop.pro"))
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
