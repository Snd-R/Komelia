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
//include(":komelia-db:wasm")
include(":komelia-jni")
include(":komelia-webview")
include(":komelia-image-decoder")
include(":wasm-image-worker")

includeBuild("third_party/secret-service") {
    dependencySubstitution { substitute(module("de.swiesend:secret-service")) }
}
includeBuild("third_party/compose-sonner") {
    dependencySubstitution { substitute(module("io.github.dokar3:sonner")).using(project(":sonner")) }
}
includeBuild("third_party/ChipTextField") {
    dependencySubstitution { substitute(module("io.github.dokar3:chiptextfield-m3")).using(project(":chiptextfield-m3")) }
    dependencySubstitution { substitute(module("io.github.dokar3:chiptextfield-core")).using(project(":chiptextfield-core")) }
}
