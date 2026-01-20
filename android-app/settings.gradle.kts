pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("multiplatform") version "1.9.22"
        kotlin("plugin.serialization") version "1.9.22"
        id("com.android.application") version "8.2.2"
        id("com.android.library") version "8.2.2"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PetCare"
include(":app")
// TODO: Fix shared module KMP configuration before re-enabling
// include(":shared")
