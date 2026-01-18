dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
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
include("website")
includeBuild("build-logic")
