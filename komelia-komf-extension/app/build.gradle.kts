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

tasks {
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

    val assembleExtension = register<Sync>("assembleExtension") {
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

        doLast {
            copy {
                from("$resourceFolder/manifest.json")
                val wasmFiles = fileTree(extensionFolder)
                    .filter { it.name.endsWith(".wasm") }
                    .joinToString(",\n        ") { "\"${it.name}\"" }
                filter(ReplaceTokens::class, "tokens" to mapOf("wasmFiles" to wasmFiles))
                into(extensionFolder)
            }
        }
    }

    val assembleExtensionDev = register<Sync>("assembleExtensionDev") {
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

        doLast {
            copy {
                from("$resourceFolder/manifest.json")
                val wasmFiles = fileTree(extensionFolderDev)
                    .filter { it.name.endsWith(".wasm") }
                    .joinToString(",\n        ") { "\"${it.name}\"" }
                filter(ReplaceTokens::class, "tokens" to mapOf("wasmFiles" to wasmFiles))
                into(extensionFolderDev)
            }
        }
    }

    val packageExtension = register<Zip>("packageExtension") {
        group = "browser-extension"
        dependsOn(assembleExtension)
        archiveFileName.set("webextension.zip")
        from(extensionFolder)
    }

    val packageExtensionDev = register<Zip>("packageExtensionDev") {
        group = "browser-extension"
        dependsOn(assembleExtensionDev)
        archiveFileName.set("webextension-dev.zip")
        from(extensionFolderDev)
    }
}
