plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
}

gradlePlugin {
    plugins {
        create("metadataTask") {
            id = "tasks.metadata"
            implementationClass = "MetadataPlugin"
        }
    }
}