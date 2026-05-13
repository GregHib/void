dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "void"

include("game")
include("cache")
include("engine")
include("tools")
include("buffer")
include("network")
include("types")
include("config")
include("database")
includeBuild("build-logic")
