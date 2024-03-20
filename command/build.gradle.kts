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
    compileOnly(project(path = ":internal", configuration = "shadow"))
    compileOnly(project(path = ":container", configuration = "shadow"))

    implementation("cloud.commandframework:cloud-paper:1.8.4")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4") {
        exclude("net.kyori")
    }
    implementation("cloud.commandframework:cloud-annotations:1.8.4")

    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("cloud.commandframework", "${internal}.commands")
        relocate("io.leangen.geantyref", "${internal}.geantyref")
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