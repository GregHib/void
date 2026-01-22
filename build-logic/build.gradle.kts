plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("world.gregs.config:config")
    implementation(libs.kotlin.embeddable)
}

gradlePlugin {
    plugins {
        create("metadataTask") {
            id = "tasks.metadata"
            implementationClass = "MetadataPlugin"
        }
    }
}