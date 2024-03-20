buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

val rootProject = project(":")

group = "${rootProject.group}.${rootProject.name.toLowerCase().split('-')[0]}"
version = rootProject.version

val internal = "${project.group}.internal"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    implementation("com.github.retrooper.packetevents:spigot:2.2.1")
    implementation("io.projectreactor:reactor-core:3.6.1")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.11.1") // reactor relocate
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8") {
        exclude(module = "checker-qual")
        exclude(module = "error_prone_annotations")
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.github.retrooper.packetevents", "${internal}.packetevents.api")
        relocate("io.github.retrooper.packetevents", "${internal}.packetevents.impl")
        relocate("net.kyori", "${internal}.kyori")
        relocate("com.google.gson", "${internal}.gson")
        relocate("com.github.benmanes.caffeine.cache", "${internal}.cache")
        relocate("org.reactivestreams", "${internal}.reactivestreams")
        relocate("reactor.", "${internal}.reactor.") // Dot in name to be safe
        relocate("com.mongodb", "${internal}.mongo")
        relocate("org.bson", "${internal}.bson")
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}