buildscript {
    dependencies {
        classpath(libs.kotlin)
        classpath(libs.shadow)
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
    implementation(libs.shadow)
    implementation(libs.spotless)
    implementation(libs.kotlin.embeddable)
}
