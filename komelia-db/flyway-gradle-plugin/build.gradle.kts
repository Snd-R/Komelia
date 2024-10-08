plugins {
    id("java")
    id("groovy")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "org.flywaydb"

repositories {
    mavenLocal()
    mavenCentral()
}


dependencies {
    implementation("org.flywaydb:flyway-core:10.19.0")
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        register("simplePlugin") {
            id = "org.flywaydb.flyway"
            displayName = "flyway"
            implementationClass = "org.flywaydb.gradle.FlywayPlugin"
        }
    }
}