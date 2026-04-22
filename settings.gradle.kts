pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OraTrackA"

include(":app")
include(":core-contracts")
include(":feature-player")
include(":feature-camera")
include(":data-session")

project(":core-contracts").projectDir = file("core-contracts")
project(":feature-player").projectDir = file("feature-player")
project(":feature-camera").projectDir = file("feature-camera")
project(":data-session").projectDir = file("data-session")
