plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("world.gregs.config:config")
    implementation("world.gregs.voidps.buffer:buffer")
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