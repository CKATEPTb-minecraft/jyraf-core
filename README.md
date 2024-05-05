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
<a href="https://repo.jyraf.com/service/rest/v1/search/assets/download?sort=version&repository=maven-snapshots&maven.groupId=dev.ckateptb.minecraft&maven.artifactId=Jyraf-Core&maven.extension=jar" target="_blank"><img alt="Download" src="https://img.shields.io/nexus/s/dev.ckateptb.minecraft/Jyraf-Core?server=https%3A%2F%2Frepo.jyraf.com"></a>
</p>

------

# Versioning

We use [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html) to manage our releases.

# Features
- [X] Easy to use
- [X] Spring-like IoC
  - [X] Component registration handlers
  - [X] Container initialization handlers
  - [X] Qualifiers
  - [X] PostConstruct
  - [X] Scheduler
  - [X] Listeners
  - [X] Annotation based commands
  - [X] Annotation based configuration
    - [X] YAML
    - [X] JSON
    - [X] HOCON
  - [X] Database
    - [X] SQLite
    - [X] H2
    - [X] MySQL
    - [X] PostgreSQL
    - [X] MariaDB
    - [X] MongoDB
    - [ ] Redis
- [X] Serializers
  - [X] Item
  - [X] World
  - [X] Location
  - [X] Duration
  - [X] Uuid
  - [X] Enum
- [X] Colliders
  - [X] Types
    - [X] AABB
    - [X] OBB
    - [X] SPHERE
    - [X] DISK
    - [X] RAY
  - [X] Intersect
    - [X] Entity
    - [X] Block
    - [X] Point
    - [X] Other collider
- [X] Text to Component mapper
  - [X] MiniMessage (with [addon](https://github.com/CKATEPTb-minecraft/jMessage))
  - [X] InkyMessage (java 17 and above)
  - [X] MineDown
- [X] Frame-based inventory user interfaces
  - [X] Anvil
  - [X] Chest
    - [X] Item
    - [X] Button
    - [X] Pagination
    - [X] Conditional
- [X] Immutable vector implementation with great functionality
- [X] World Repository for optimized and thread-safe lookups
  - [X] Entity
    - [X] Async
    - [X] Sync (with [addon](https://github.com/CKATEPTb-minecraft/jaser), not recommended)
  - [X] Packet entity
    - [X] All types
    - [X] All meta
    - [X] Dynamic properties
    - [X] Goals
      - [X] Fall
      - [X] Look
      - [X] Move
      - [ ] Passengers
  - [ ] Packet hologram
  - [X] Packet block
- [ ] Packet scoreboards
- [ ] Packet boss bars 
- [ ] Reversible system of temporary mechanics with a return queue.

***
# Usage 
### Visit the [wiki](https://github.com/CKATEPTb-minecraft/jyraf-core/wiki) to start using Jyraf right now!
***

# Credits
- The [contributors](https://github.com/CKATEPTb-minecraft/jyraf-core/graphs/contributors) of the project
- [Tofaa2](https://github.com/Tofaa2/) for [EntityMeta](https://github.com/Tofaa2/EntityLib)

# License
This project is licensed under the [GPL-3.0 license](https://github.com/CKATEPTb-minecraft/jyraf-core/blob/development/LICENSE.md).