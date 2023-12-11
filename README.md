<p align="center">
<h3 align="center">TableclothPluginBlank</h3>

------

<p align="center">
Project template aimed at creating minecraft plugins with the ability to publish to a maven repository. It is useful for people with basic understanding of java, gradle, workflow and is designed for lazy people
</p>

<p align="center">
<img alt="License" src="https://img.shields.io/github/license/CKATEPTb-minecraft/TableclothPluginBlank">
<a href="https://docs.gradle.org/7.5/release-notes.html"><img src="https://img.shields.io/badge/Gradle-7.5-brightgreen.svg?colorB=469C00&logo=gradle"></a>
<a href="https://discord.gg/P7FaqjcATp" target="_blank"><img alt="Discord" src="https://img.shields.io/discord/925686623222505482?label=discord"></a>
</p>

------

# Versioning

We use [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html) to manage our releases.

# Features

- [X] Automatic compression
- [X] Easy to use
- [X] Automatic publishing to Nexus repo
- [X] Ability to inject dependencies
- [X] Documented

# How To

* Fork this project
* Allow github-actions on your fork
* Open `settings.gradle.kts` and configure project name 
* Open `build.gradle.kts` and follow todos
* Open `./github/workflows/publish.yml` and follow todos or delete this workflow
* Open `./src/main/resource/plugin.yml` and follow todos
* Rename project package and main class
* Change `LICENSE` if you need
* Modify the README to suit your needs so that it describes your project well. You can use [shields.io](https://shields.io)
* Create separate branches for your drafts, and push from to the `development` branch only if you're confident in your drafts
* Use the `development` branch as the branch where your drafts are built
* Push the `development` branch to the `production` branch only if you are sure about the stability of the first
* Start work