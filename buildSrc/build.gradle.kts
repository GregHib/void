buildscript {
    dependencies {
        classpath(libs.kotlin)
        classpath(libs.shadow)
        classpath("world.gregs.config:config")
    }
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.jacoco.aggregation)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.spotless)
}
