buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow").version("7.1.0")
    id("io.github.gradle-nexus.publish-plugin").version("1.1.0")
    // https://github.com/PaperMC/paperweight
//    id("io.papermc.paperweight.userdev").version("1.5.11")
}
group = "dev.ckateptb.minecraft"
version = "1.17.0-SNAPSHOT"

val rootPackage = "${project.group}.${project.name.toLowerCase().split('-')[0]}"
val internal = "${rootPackage}.internal"

repositories {
    mavenCentral()
//    maven("https://repo.jyraf.com/repository/maven-snapshots/")
    maven("https://repo.glowing.ink/snapshots")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.minebench.de/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/nms/")
}

dependencies {
//    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")

    // Non-blocking threads
    implementation("io.projectreactor:reactor-core:3.6.1")
    // High performance cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8") {
        exclude(module = "checker-qual")
        exclude(module = "error_prone_annotations")
    }
    // Text Components
    implementation("ink.glowing:inkymessage:0.12.0-SNAPSHOT")
    implementation("de.themoep:minedown-adventure:1.7.1-SNAPSHOT")
    // Reflection
    implementation("org.jooq:joor:0.9.15")
    // Additional date-time
    implementation("org.threeten:threeten-extra:1.7.2")
    // Anvil IUI
    implementation("net.wesjd:anvilgui:1.9.2-SNAPSHOT")
    // PathFinder
    implementation("com.github.patheloper.pathetic:pathetic-mapping:2.4")
    // PersistentDataContainerSerializer
    implementation("com.jeff-media:persistent-data-serializer:1.0")
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.5")

    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.j256.ormlite", "${internal}.ormlite")
        relocate("com.zaxxer.hikari", "${internal}.hikari")
        relocate("ink.glowing.text", "${internal}.ink")
        relocate("org.joor", "${internal}.reflection")
        relocate("org.threeten.extra", "${internal}.time")
        relocate("net.wesjd.anvilgui", "${internal}.anvil")
        relocate("org.json", "${internal}.json")
        relocate("de.themoep.minedown.adventure", "${internal}.minedown")
        relocate("org.patheloper", "${internal}.pathetic")
        relocate("com.jeff_media.persistentdataserializer", "${internal}.persistentdataserializer")
    }
    build {
        dependsOn(/*reobfJar,*/ shadowJar)
    }
    publish {
        dependsOn(/*reobfJar,*/ shadowJar)
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
                    "projectMainClass" to "${rootPackage}.${project.name.split('-')[0]}"
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
        create<MavenPublication>("mavenJava") {
            artifact(tasks.getByName("shadowJar").outputs.files.singleFile)
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