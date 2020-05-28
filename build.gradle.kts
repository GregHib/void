buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.71"))
    }
}

plugins {
    kotlin("jvm") version "1.3.71"
}

val koinVersion = "2.1.5"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "rs.dusk"
    version = "0.0.2"

    java.sourceCompatibility = JavaVersion.VERSION_11

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://repo.maven.apache.org/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.bintray.com/michaelbull/maven")
    }

    dependencies {
        //Main
	    implementation(group="rs.dusk.core", name="network", version="0.1.1")
	    implementation(group="rs.dusk.core", name="utility", version="0.1.1")
	    
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.netty:netty-all:4.1.44.Final")
        implementation(group = "com.displee", name = "rs-cache-library", version = "6.3")
        implementation(group = "org.yaml", name = "snakeyaml", version = "1.8")
        implementation(group = "io.github.classgraph", name = "classgraph", version = "4.6.3")
        implementation(group = "com.michael-bull.kotlin-inline-logger", name = "kotlin-inline-logger-jvm", version = "1.0.2")
        implementation(group = "org.koin", name = "koin-core", version = koinVersion)
        implementation(group = "org.koin", name = "koin-logger-slf4j", version = koinVersion)
        implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.5")

        //Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")

        //Utilities
        implementation("com.google.guava:guava:19.0")
        implementation("org.apache.commons:commons-lang3:3.0")
        implementation("org.jetbrains.exposed:exposed-core:0.22.1")
        implementation("org.jetbrains.exposed:exposed-jdbc:0.22.1")
        implementation("com.fasterxml.jackson.core:jackson-core:2.10.3")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.3")
        implementation("org.postgresql:postgresql:42.2.11")
        implementation("it.unimi.dsi:fastutil:8.3.1")

        //Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.1")
        testImplementation(group = "org.koin", name = "koin-test", version = koinVersion)
        testImplementation(group = "io.mockk", name = "mockk", version = "1.9.3")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

}
