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
    id("io.papermc.paperweight.userdev").version("1.3.8")
}
// TODO Change the group to the one you need
group = "dev.ckateptb.minecraft"
// TODO Control project version according to https://semver.org/spec/v2.0.0.html
version = "1.0.0-SNAPSHOT"

val rootPackage = "${project.group}.${project.name.toLowerCase()}"
val internal = "${rootPackage}.internal"

repositories {
    mavenCentral()
    // TODO You can add the repositories you need
//    maven("https://repo.animecraft.fun/repository/maven-snapshots/")
}

dependencies {
    // TODO Configure papermc version
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")

    // TODO Using the line below you can add dependencies.
    //  Plus, instead of the version, it will give you the latest version.
//    implementation("com.example.group:example-library:+")
    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

tasks {
    shadowJar {
        // TODO If you need to embed an external library, specify its initial package instead of <com> (2 places)
//        relocate("com", "${internal}.com")
//        ...
//        relocate("com", "${internal}.com")
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
//        dependsOn(reobfJar, shadowJar)
        // Comment next line if u need only embed, without shrink
        dependsOn(reobfJar, "shrink")
    }
    publish {
        // Uncomment next line if u need only embed
//        dependsOn(reobfJar, shadowJar)
        // Comment next line if u need only embed, without shrink
        dependsOn(reobfJar, "shrink")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand(
                "projectVersion" to project.version,
                "projectName" to project.name,
                "projectMainClass" to "${rootPackage}.${project.name}"
            )
        }
        from("LICENSE") {
            rename { "${project.name.toUpperCase()}_${it}" }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
        create("myNexus") {
            // TODO Customize maven-publish to suit your needs.
            //  As an example, here is the setting for nexus + github-action.
            //  For the latter, you need to configure github-secrets
            nexusUrl.set(uri("https://repo.animecraft.fun/"))
            snapshotRepositoryUrl.set(uri("https://repo.animecraft.fun/repository/maven-snapshots/"))
            username.set(System.getenv("NEXUS_USERNAME"))
            password.set(System.getenv("NEXUS_PASSWORD"))
        }
    }
}