import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.snd_r.komelia.db.sqlite"
version = "unspecified"

kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    android {
        namespace = "io.github.snd_r.komelia.infra.database.sqlite"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
        androidResources { enable = true }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            implementation(projects.komeliaDomain.core)
            implementation(projects.komeliaDomain.offline)
            implementation(projects.komeliaDomain.komgaApi)
            implementation(projects.komeliaInfra.database.shared)
            implementation(projects.komeliaInfra.imageDecoder.shared)

            implementation(libs.compose.runtime)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.filekit.core)
            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.json)
            implementation(libs.exposed.kotlin.datetime)
            implementation(libs.hikariCP)
            implementation(libs.flyway.core)
            implementation(libs.sqlite.xerial.jdbc)
        }
    }
}

val sqliteExtract: Configuration by configurations.creating
dependencies { sqliteExtract(libs.sqlite.xerial.jdbc) }
tasks.register<Sync>("android-arm64-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/aarch64/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/arm64-v8a")
}

tasks.register<Sync>("android-armv7a-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/arm/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/armeabi-v7a")
}

tasks.register<Sync>("android-x86_64-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/x86_64/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/x86_64")
}

tasks.register<Sync>("android-x86-ExtractSqliteLib") {
    val sqliteJar = configurations.getByName("sqliteExtract").first()
    val file = zipTree(sqliteJar.absolutePath)
        .matching { include("org/sqlite/native/Linux-Android/x86/libsqlitejdbc.so") }
        .singleFile
    from(file)
    into("$projectDir/src/androidMain/jniLibs/x86")
}

afterEvaluate {
    val extractTasks = listOf(
        "android-arm64-ExtractSqliteLib",
        "android-armv7a-ExtractSqliteLib",
        "android-x86_64-ExtractSqliteLib",
        "android-x86-ExtractSqliteLib"
    )
    tasks.named("preBuild") {
        dependsOn(extractTasks)
    }
}
