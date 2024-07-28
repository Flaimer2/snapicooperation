plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.plugin.yml.bukkit)
    alias(libs.plugins.kapt)
    `maven-publish`
}

group = "ru.snapix"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    kapt(libs.velocityapi)
    compileOnly(libs.bukkit)
    compileOnly(libs.velocityapi)
    compileOnly(libs.snapilibrary.bukkit)
    compileOnly(libs.snapilibrary.velocity)
    compileOnly(libs.serialization)
    compileOnly(libs.luckperms)
    compileOnly(libs.balancer)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.profile)
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
}

bukkit {
    main = "ru.snapix.snapicooperation.SnapiCooperation"
    author = "SnapiX"
    website = "https://mcsnapix.ru"
    depend = listOf("SnapiLibrary")
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