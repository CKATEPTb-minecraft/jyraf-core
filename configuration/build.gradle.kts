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
    maven("https://repo.codemc.io/repository/nms/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(path = ":container", configuration = "shadow"))
    implementation("org.spongepowered:configurate-gson:4.1.2")
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation("org.spongepowered:configurate-yaml:4.1.2") {
        exclude(module = "snakeyaml")
    }

    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.google.gson", "${internal}.gson")
        relocate("com.typesafe.config", "${internal}.typesafe")
        relocate("io.leangen.geantyref", "${internal}.geantyref")
        relocate("org.spongepowered.configurate", "${internal}.configurate")
        minimize()
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