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
include(":komelia-domain:core")
include(":komelia-domain:offline")
include(":komelia-domain:komga-api")
include(":komelia-ui")

include(":komelia-infra:database:transaction")
include(":komelia-infra:database:shared")
include(":komelia-infra:database:sqlite")
include(":komelia-infra:database:wasm")
include(":komelia-infra:image-decoder:shared")
include(":komelia-infra:image-decoder:vips")
include(":komelia-infra:image-decoder:wasm-image-worker")
include(":komelia-infra:jni")
include(":komelia-infra:onnxruntime:api")
include(":komelia-infra:onnxruntime:jvm")
include(":komelia-infra:webview")

include(":komelia-komf-extension:app")
include(":komelia-komf-extension:content")
include(":komelia-komf-extension:background")
include(":komelia-komf-extension:popup")
include(":komelia-komf-extension:shared")

include(":third_party:ChipTextField:chiptextfield-core")
include(":third_party:ChipTextField:chiptextfield-m3")
include(":third_party:compose-sonner:sonner")
include(":third_party:indexeddb:core")
include(":third_party:indexeddb:external")

includeBuild("third_party/secret-service") {
    dependencySubstitution { substitute(module("de.swiesend:secret-service")) }
}
