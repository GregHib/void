buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
}

val koinVersion = "2.2.1"
val junitVersion = "5.6.2"
val exposedVersion = "0.24.1"
val jacksonVersion = "2.11.0"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "rs.dusk"
    version = "0.0.2"

    java.sourceCompatibility = JavaVersion.VERSION_14

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
        implementation(group = "rs.dusk.core", name = "network", version = "0.1.2")
        implementation(group = "rs.dusk.core", name = "utility", version = "0.1.2")

        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.netty:netty-all:4.1.44.Final")
        implementation(group = "com.displee", name = "rs-cache-library", version = "6.4")
        implementation(group = "org.yaml", name = "snakeyaml", version = "1.26")
        implementation(group = "io.github.classgraph", name = "classgraph", version = "4.8.78")
        implementation(
            group = "com.michael-bull.kotlin-inline-logger",
            name = "kotlin-inline-logger-jvm",
            version = "1.0.2"
        )
        implementation(group = "org.koin", name = "koin-core", version = koinVersion)
        implementation(group = "org.koin", name = "koin-logger-slf4j", version = koinVersion)
        implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.7")

        //Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")

        //Utilities
        implementation("com.google.guava:guava:29.0-jre")
        implementation("org.apache.commons:commons-lang3:3.10")
        implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
        implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
        implementation("org.postgresql:postgresql:42.2.12")
	    implementation("com.zaxxer:HikariCP:3.4.5")
        implementation("it.unimi.dsi:fastutil:8.3.1")

        //Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation(group = "org.koin", name = "koin-test", version = koinVersion)
        testImplementation(group = "io.mockk", name = "mockk", version = "1.10.0")
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
