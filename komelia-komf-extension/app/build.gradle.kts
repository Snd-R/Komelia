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
            implementation(projects.komeliaKomfExtension.content)
            implementation(projects.komeliaKomfExtension.popup)
        }
    }
}

interface Injected {
    @get:Inject
    val fs: FileSystemOperations
}

val projectPrefix = ":komelia-komf-extension"
val content = "$projectPrefix:content"
val popup = "$projectPrefix:popup"
val background = "$projectPrefix:background"
val resourceFolder = "src/wasmJsMain/resources"

enum class EnvType(
    val postfix: String,
    val extensionBuildDir: String,
//    val extensionOutDir: String,
    val extensionDepTask: String,
    val manifest: String,
) {
    PROD_CHROME(
        "prod_chrome",
        "productionExecutable",
//        "extension",
        "wasmJsBrowserDistribution",
        "manifest_chrome.json"
    ),
    PROD_FIREFOX(
        "prod_firefox",
        "productionExecutable",
//        "extension",
        "wasmJsBrowserDistribution",
        "manifest_firefox.json"
    ),

    DEV_CHROME(
        "dev_chrome",
        "developmentExecutable",
//        "extensionDev",
        "wasmJsBrowserDevelopmentExecutableDistribution",
        "manifest_chrome.json"
    ),
    DEV_FIREFOX(
        "dev_firefox",
        "developmentExecutable",
//        "extensionDev",
        "wasmJsBrowserDevelopmentExecutableDistribution",
        "manifest_firefox.json"
    ),
}
EnvType.entries.forEach { env ->
    tasks.register<Sync>("assembleExtension_${env.postfix}") {
        val outputDir = layout.buildDirectory.dir("extension_" + env.postfix)
        group = "browser-extension"
        val contentInput = "$projectDir/../content/build/dist/wasmJs/${env.extensionBuildDir}/"
        val contentStringsInput =
            "$projectDir/../content/build/dist/wasmJs/${env.extensionBuildDir}/composeResources/io.github.snd_r.komelia.ui.komelia_ui.generated.resources/values/"
        val popupInput = "$projectDir/../popup/build/dist/wasmJs/${env.extensionBuildDir}/"
        val backgroundInput = "$projectDir/../background/build/dist/wasmJs/${env.extensionBuildDir}/"

        inputs.dir("$resourceFolder/icons")
        inputs.dir("$resourceFolder/html")
        inputs.dir(contentInput)
        inputs.dir(contentStringsInput)
        inputs.dir(popupInput)
        inputs.dir(backgroundInput)
        outputs.dir(outputDir)
        dependsOn(
            content + ":${env.extensionDepTask}",
            popup + ":${env.extensionDepTask}",
            background + ":${env.extensionDepTask}"
        )
        from(contentInput) {
            include("*.wasm", "*.js")
            exclude("publicPath.js")
        }
        from(contentStringsInput) {
            include("*.cvr")
        }
        from(popupInput) {
            include("*.wasm", "*.js")
            exclude("publicPath.js")
        }
        from(backgroundInput) {
            include("*.wasm", "*.js")
            exclude("publicPath.js")
        }

        from(
            "$resourceFolder/icons",
            "$resourceFolder/html",
        )
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        into(outputDir)

        val injectedFs = project.objects.newInstance<Injected>()
        val fileTree = project.objects.fileTree().from(outputDir)
        val resourceFolder = resourceFolder
        doLast {
            injectedFs.fs.copy {
                val wasmFiles = fileTree.filter { it.name.endsWith(".wasm") }
                    .joinToString(",\n        ") { "\"${it.name}\"" }
                from("$resourceFolder/${env.manifest}")
                rename { "manifest.json" }

                filter(ReplaceTokens::class, "tokens" to mapOf("wasmFiles" to wasmFiles))
                into(outputDir)

            }
        }
    }
}
EnvType.entries.forEach { env ->
    tasks.register<Zip>("packageExtension_${env.postfix}") {
        val extensionDir = layout.buildDirectory.dir("extension_" + env.postfix)
        group = "browser-extension"
        dependsOn("assembleExtension_${env.postfix}")
        archiveFileName.set("webextension_${env.postfix}.zip")
        from(extensionDir)
    }
}
