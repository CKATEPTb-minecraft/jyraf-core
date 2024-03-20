buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

group = "dev.ckateptb.minecraft"
version = "2.2.1-SNAPSHOT"

val rootPackage = "${project.group}.${project.name.toLowerCase().split('-')[0]}"
val internal = "${rootPackage}.internal"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly(":jyraf-core")
    // Packets
    implementation("com.github.retrooper.packetevents:spigot:2.2.1")
}
tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.github.retrooper.packetevents", "dev.ckateptb.minecraft.packetevents.api")
        relocate("io.github.retrooper.packetevents", "dev.ckateptb.minecraft.packetevents.impl")
        relocate("net.kyori", "${internal}.kyori")
        relocate("com.google.gson", "${internal}.gson")
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