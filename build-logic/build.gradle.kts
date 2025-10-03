plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
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