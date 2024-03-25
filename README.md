<p align="center">
<h3 align="center">jyraf-core</h3>

------

<p align="center">
Jyraf-Core is an advanced plugin development library with the goal of providing highly efficient tools to speed up plugin development and optimize plugin performance. This library perfectly combines ease of use, maximum performance and a wide range of features, making the process of creating Minecraft plugins more convenient and efficient.
</p>

<p align="center">
<img alt="License" src="https://img.shields.io/github/license/CKATEPTb-minecraft/jyraf-core">
<a href="https://docs.gradle.org/7.5/release-notes.html"><img src="https://img.shields.io/badge/Gradle-7.4-brightgreen.svg?colorB=469C00&logo=gradle"></a>
<a href="https://discord.gg/P7FaqjcATp" target="_blank"><img alt="Discord" src="https://img.shields.io/discord/925686623222505482?label=discord"></a>
<a href="https://repo.jyraf.com/service/rest/v1/search/assets/download?sort=version&repository=maven-snapshots&maven.groupId=dev.ckateptb.minecraft&maven.artifactId=Jaser&maven.extension=jar" target="_blank"><img alt="Download" src="https://img.shields.io/nexus/s/dev.ckateptb.minecraft/Jyraf-Core?server=https%3A%2F%2Frepo.jyraf.com"></a>
</p>

------

# Versioning

We use [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html) to manage our releases.

# Features
- [X] Easy to use
- [X] Spring-like IoC
- [X] Class to configuration file (yaml, json, hocon)
- [X] Annotation based commands
- [X] Excellent type serializers(with prebuilt item, world, duration, location, uuid, enum, etc.)
- [X] High performance cache
- [X] Database (SQLite, H2, MySQL, PostgreSQL, MariaDB, MongoDB, ~~Redis~~)
- [X] Colliders (AABB, OBB, SPHERE, RAY, DISK, etc.)
- [X] User-friendly Components (MiniMessage, MineDown, InkyMessage)
- [X] Frame-based inventory user interfaces (Chest, Anvil, Button, Pagination)
- [X] Immutable vector implementation with great functionality
- [X] World Repository for optimized lookups( with prebuilt entity, packet entity, ~~block~~, etc.)
- [X] PacketEntity (any type, move, look, gravity, pathfinder, ~~traits~~, ~~nametag~~, ~~hologram~~)
- [ ] Packet blocks
- [ ] Packet scoreboards
- [ ] Packet boss bars 
- [ ] Reversible system of temporary mechanics with a return queue.

# Download

Download from our repository or depend via Gradle:

```kotlin
repositories {
    maven("https://repo.jyraf.com/repository/maven-snapshots/")
}

dependencies {
    implementation("dev.ckateptb.minecraft:Jyraf-Core:<version>")
}
```

# How To

* Import the dependency [as shown above](#Download)
* Add jyraf-core as a dependency to your `plugin.yml`
```yaml
name: ...
version: ...
main: ...
depend: [ "Jyraf-Core" ]
authors: ...
description: ...
```
* The capabilities of jyraf-core are so great that you will have to wait until I make a wiki or figure it out yourself.