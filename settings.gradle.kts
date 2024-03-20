pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "Jyraf-Core"
include("container", "configuration", "database", "command", "plugin", "serializer", "internal")
