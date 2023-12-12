import proguard.gradle.ProGuardTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.1")
    }
}

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow").version("7.1.0")
    id("io.github.gradle-nexus.publish-plugin").version("1.1.0")
    // https://github.com/PaperMC/paperweight
    id("io.papermc.paperweight.userdev").version("1.5.11")
}
group = "dev.ckateptb.minecraft"
version = "1.0.0-SNAPSHOT"

val rootPackage = "${project.group}.${project.name.toLowerCase().split('-')[0]}"
val internal = "${rootPackage}.internal"

repositories {
    mavenCentral()
//    maven("https://repo.jyraf.com/repository/maven-snapshots/")
//    maven("https://repo.codemc.io/repository/nms/")
}

dependencies {
    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    // Non-blocking threads
    implementation("io.projectreactor:reactor-core:3.6.1")
    // High performance cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8") {
        exclude(module = "checker-qual")
    }
    // Configuration
    implementation("org.spongepowered:configurate-gson:4.1.2")
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation("org.spongepowered:configurate-jackson:4.1.2")
    implementation("org.spongepowered:configurate-xml:4.1.2")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    // Commands
    implementation("cloud.commandframework:cloud-bukkit:2.0.0-SNAPSHOT")
    implementation("cloud.commandframework:cloud-annotations:2.0.0-SNAPSHOT")

//    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

tasks {
    shadowJar {
//        relocate("org.reactivestreams", "${rootPackage}.reactivestreams")
//        relocate("reactor", "${rootPackage}.reactor")
//        relocate("com.github.benmanes.caffeine.cache", "${rootPackage}.cache")
//        relocate("com.google.errorprone", "${internal}.errorprone")
    }
    register<ProGuardTask>("shrink") {
        dependsOn(shadowJar)
        injars(shadowJar.get().outputs.files)
        outjars("${project.buildDir}/libs/${project.name}-${project.version}.jar")

        ignorewarnings()

        libraryjars("${System.getProperty("java.home")}/jmods")

        keep(mapOf("includedescriptorclasses" to true), "public class !${internal}.** { *; }")
        keepattributes("RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations")

        dontobfuscate()
        dontoptimize()
    }
    build {
        // Uncomment next line if u need only embed, without shrink
        dependsOn(reobfJar, shadowJar)
        // Comment next line if u need only embed, without shrink
//        dependsOn(reobfJar, "shrink")
    }
    publish {
        // Uncomment next line if u need only embed
        dependsOn(reobfJar, shadowJar)
        // Comment next line if u need only embed, without shrink
//        dependsOn(reobfJar, "shrink")
    }
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand(
                "projectVersion" to project.version,
                "projectName" to project.name,
                "projectMainClass" to "${rootPackage}.Jyraf"
            )
        }
        from("LICENSE") {
            rename { "${project.name.toUpperCase()}_${it}" }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

publishing {
    publications {
        publications.create<MavenPublication>("mavenJava") {
            artifacts {
                artifact(tasks.getByName("shrink").outputs.files.singleFile)
            }
        }
    }
}

nexusPublishing {
    repositories {
        create("jyrafRepo") {
            nexusUrl.set(uri("https://repo.jyraf.com/"))
            snapshotRepositoryUrl.set(uri("https://repo.jyraf.com/repository/maven-snapshots/"))
            username.set(System.getenv("NEXUS_USERNAME"))
            password.set(System.getenv("NEXUS_PASSWORD"))
        }
    }
}