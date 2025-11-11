@file:OptIn(ExperimentalWasmDsl::class)

import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    wasmJs {
        outputModuleName = "app"
        binaries.executable()
        browser()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":komelia-komf-extension:content"))
            implementation(project(":komelia-komf-extension:popup"))
        }
    }
}

interface Injected {
    @get:Inject
    val fs: FileSystemOperations
}

val projectPrefix = ":komelia-komf-extension"
val content = "$projectPrefix:content:wasmJsBrowserDistribution"
val popup = "$projectPrefix:popup:wasmJsBrowserDistribution"
val background = "$projectPrefix:background:wasmJsBrowserDistribution"

val contentDev = "$projectPrefix:content:wasmJsBrowserDevelopmentExecutableDistribution"
val popupDev = "$projectPrefix:popup:wasmJsBrowserDevelopmentExecutableDistribution"
val backgroundDev = "$projectPrefix:background:wasmJsBrowserDevelopmentExecutableDistribution"

val extensionFolder = "$projectDir/build/extension"
val extensionFolderDev = "$projectDir/build/extensionDev"
val resourceFolder = "src/wasmJsMain/resources"

tasks.register<Sync>("assembleExtension") {
    group = "browser-extension"
    dependsOn(content, popup, background)
    from(
        "$projectDir/../content/build/dist/wasmJs/productionExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }
    from(
        "$projectDir/../popup/build/dist/wasmJs/productionExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }
    from(
        "$projectDir/../background/build/dist/wasmJs/productionExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }

    from(
        "$resourceFolder/icons",
        "$resourceFolder/html",
    )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    into(extensionFolder)

    val injectedFs = project.objects.newInstance<Injected>()
    val fileTree = project.objects.fileTree().from(extensionFolder)
    val extensionFolder = extensionFolder
    val resourceFolder = resourceFolder
    doLast {
        injectedFs.fs.copy {
            val wasmFiles = fileTree.filter { it.name.endsWith(".wasm") }
                .joinToString(",\n        ") { "\"${it.name}\"" }
            from("$resourceFolder/manifest.json")
            filter(ReplaceTokens::class, "tokens" to mapOf("wasmFiles" to wasmFiles))
            into(extensionFolder)

        }
    }
}

tasks.register<Sync>("assembleExtensionDev") {
    group = "browser-extension"
    dependsOn(contentDev, popupDev, backgroundDev)
    from(
        "$projectDir/../content/build/dist/wasmJs/developmentExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }
    from(
        "$projectDir/../popup/build/dist/wasmJs/developmentExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }
    from(
        "$projectDir/../background/build/dist/wasmJs/developmentExecutable/",
    ) {
        include("*.wasm", "*.js")
        exclude("publicPath.js")
    }

    from(
        "$resourceFolder/icons",
        "$resourceFolder/html",
    )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    into(extensionFolderDev)

    val injectedFs = project.objects.newInstance<Injected>()
    val fileTree = project.objects.fileTree().from(extensionFolder)
    val extensionFolderDev = extensionFolderDev
    val resourceFolder = resourceFolder
    doLast {
        injectedFs.fs.copy {
            val wasmFiles = fileTree.filter { it.name.endsWith(".wasm") }
                .joinToString(",\n        ") { "\"${it.name}\"" }
            from("$resourceFolder/manifest.json")
            filter(ReplaceTokens::class, "tokens" to mapOf("wasmFiles" to wasmFiles))
            into(extensionFolderDev)
        }
    }
}

tasks.register<Zip>("packageExtension") {
    group = "browser-extension"
    dependsOn("assembleExtension")
    archiveFileName.set("webextension.zip")
    from(extensionFolder)
}

tasks.register<Zip>("packageExtensionDev") {
    group = "browser-extension"
    dependsOn("assembleExtensionDev")
    archiveFileName.set("webextension-dev.zip")
    from(extensionFolderDev)
}

