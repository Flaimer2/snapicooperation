import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kapt)
    `maven-publish`
}

group = "ru.snapix"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
}

dependencies {
    kapt(libs.velocity.api)

    compileOnly(libs.velocity.api)
//    compileOnly(files("libs/server.jar"))
    compileOnly(libs.library)
    compileOnly(libs.serialization)
    compileOnly(libs.protocolize)
    compileOnly(libs.luckperms)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name.lowercase()
            groupId = group.toString()

            from(components["java"])
        }
    }
}
