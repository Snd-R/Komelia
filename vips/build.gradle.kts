import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("jvm") version "1.9.22"
    id("de.undercouch.download") version "5.6.0"
}

group = "io.github.snd_r"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.12")
}

kotlin {
    jvmToolchain(21)
}

val linuxBuildDir = "$projectDir/native/build"
val windowsBuildDir = "$projectDir/native/build-w64"
val classpathResourcesDir = "$projectDir/src/main/resources/"

tasks.register<Exec>("linuxPrepareBuild") {
    group = "vips"
    project.mkdir(linuxBuildDir)
    workingDir(linuxBuildDir)
    commandLine("cmake", "-G", "Ninja", "-DCMAKE_BUILD_TYPE=Release", "..")
}

tasks.register<Exec>("linuxBuild") {
    group = "vips"
    dependsOn("linuxPrepareBuild")

    workingDir(linuxBuildDir)
    commandLine("cmake", "--build", ".", "-j", "${Runtime.getRuntime().availableProcessors()}")
}

tasks.register<Sync>("linuxCopyVipsLibsToClasspath") {
    group = "vips"
    val libs = setOf(
        "libdav1d.so",
        "libexpat.so",
        "libffi.so",
        "libfftw3.so",
        "libgio-2.0.so",
        "libglib-2.0.so",
        "libgmodule-2.0.so",
        "libgobject-2.0.so",
        "libheif.so",
        "libhwy.so",
        "libjpeg.so",
        "libsharpyuv.so",
        "libspng.so",
        "libturbojpeg.so",
        "libvips.so",
        "libwebp.so",
        "libwebpdecoder.so",
        "libwebpdemux.so",
        "libwebpmux.so",
        "libkomelia.so",
        "libz.so",
    )

    from("$linuxBuildDir/fakeroot/lib/")
    into(classpathResourcesDir)
    include { it.name in libs }
}

tasks.register("linuxStripDebugSymbols") {
    group = "vips"
    dependsOn("linuxCopyVipsLibsToClasspath")
    doLast {
        fileTree(classpathResourcesDir)
            .filter { it.isFile && it.extension == "so" }
            .forEach { file ->
                exec {
                    workingDir(classpathResourcesDir)
                    commandLine("strip", "-S", file.name)
                }
            }
    }
}

tasks.register<Delete>("cleanVips") {
    group = "vips"
    delete(linuxBuildDir)
    delete(windowsBuildDir)
    delete(fileTree(classpathResourcesDir))
}

tasks.register<Download>("windowsDownloadJdk") {
    group = "vips"
    onlyIfModified(true)
    src("https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_windows_hotspot_21.0.2_13.zip")
    dest(File("$windowsBuildDir/jdk", "jdk.zip"))
}

tasks.register<Copy>("windowsUnzipJdk") {
    group = "vips"
    dependsOn("windowsDownloadJdk")
    from(zipTree("$windowsBuildDir/jdk/jdk.zip")) {
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
        }
    }
    into("$windowsBuildDir/jdk")
}

tasks.register<Exec>("windowsPrepareBuild") {
    group = "vips"
    dependsOn("windowsUnzipJdk")
    project.mkdir(windowsBuildDir)
    workingDir(windowsBuildDir)
    environment("JAVA_HOME", "$windowsBuildDir/jdk")
    commandLine(
        "cmake",
        "-DCMAKE_BUILD_TYPE=Release",
        "-DCMAKE_TOOLCHAIN_FILE=toolchain-mingw-w64-x86_64.cmake",
        ".."
    )
}

tasks.register<Exec>("windowsBuild") {
    group = "vips"
    dependsOn("windowsPrepareBuild")

    workingDir(windowsBuildDir)
    commandLine(
        "cmake",
        "--build", ".",
        "-j", "${Runtime.getRuntime().availableProcessors()}"
    )
    environment("JAVA_HOME", "$windowsBuildDir/jdk")
}

tasks.register<Sync>("windowsCopyVipsLibsToClasspath") {
    group = "vips"

    val libs = setOf(
        "libdav1d.dll",
        "libexpat-1.dll",
        "libffi-8.dll",
        "libfftw3.dll",
        "libgio-2.0-0.dll",
        "libglib-2.0-0.dll",
        "libgmodule-2.0-0.dll",
        "libgobject-2.0-0.dll",
//        "libgthread-2.0-0.dll",
        "libheif.dll",
        "libhwy.dll",
        "libintl-8.dll",
        "libjpeg-62.dll",
        "libkomelia.dll",
        "libsharpyuv.dll",
        "libspng.dll",
        "libvips-42.dll",
        "libwebp.dll",
        "libwebpdecoder.dll",
        "libwebpdemux.dll",
        "libwebpmux.dll",
        "libz1.dll"
    )

    from("$windowsBuildDir/fakeroot/bin/")
    into(classpathResourcesDir)
    include { it.name in libs }

    doLast {
        copy {
            from("/usr/x86_64-w64-mingw32/bin/")
            include("libstdc++-6.dll")
            include("libwinpthread-1.dll")
            include("libgcc_s_seh-1.dll")
            into(classpathResourcesDir)
        }
    }
}


tasks.register("windowsStripDebugSymbols") {
    group = "vips"
    dependsOn("windowsCopyVipsLibsToClasspath")
    doLast {
        fileTree(classpathResourcesDir)
            .forEach { file ->
                exec {
                    workingDir(classpathResourcesDir)
                    commandLine("x86_64-w64-mingw32-strip", "-S", file.name)
                }
            }
    }
}
