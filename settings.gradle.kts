rootProject.name = "Komelia"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":komelia-app")
include(":komelia-core")
include(":komelia-db:shared")
include(":komelia-db:sqlite")
include(":komelia-db:wasm")
include(":komelia-jni")
include(":komelia-webview")
include(":komelia-image-decoder:shared")
include(":komelia-image-decoder:vips")
include(":komelia-image-decoder:wasm-image-worker")
include(":third_party:ChipTextField:chiptextfield-core")
include(":third_party:ChipTextField:chiptextfield-m3")
include(":third_party:compose-sonner:sonner")
include(":third_party:indexeddb:core")
include(":third_party:indexeddb:external")

includeBuild("third_party/secret-service") {
    dependencySubstitution { substitute(module("de.swiesend:secret-service")) }
}
