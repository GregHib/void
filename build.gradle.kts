buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.30"))
    }
}

plugins {
    kotlin("jvm") version "1.4.30"
}

val koinVersion = "2.2.1"
val junitVersion = "5.6.2"
val jacksonVersion = "2.12.1"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "world.gregs.void"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_15

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://repo.maven.apache.org/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.bintray.com/michaelbull/maven")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.netty:netty-all:4.1.44.Final")
        implementation(group = "com.displee", name = "rs-cache-library", version = "6.7")
        implementation(group = "org.yaml", name = "snakeyaml", version = "1.26")
        implementation(
            group = "com.michael-bull.kotlin-inline-logger",
            name = "kotlin-inline-logger-jvm",
            version = "1.0.2"
        )
        implementation(group = "org.koin", name = "koin-core", version = koinVersion)
        implementation(group = "org.koin", name = "koin-logger-slf4j", version = koinVersion)
        implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.4.2")

        //Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")

        //Utilities
        implementation("com.google.guava:guava:29.0-jre")
        implementation("org.apache.commons:commons-lang3:3.10")
        implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
        implementation("it.unimi.dsi:fastutil:8.3.1")

        //Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation(group = "org.koin", name = "koin-test", version = koinVersion)
        testImplementation(group = "io.mockk", name = "mockk", version = "1.10.0")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes")
        }
    }

}
