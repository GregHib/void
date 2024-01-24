import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.21"))
    }
}

plugins {
    kotlin("jvm") version "1.8.21"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "world.gregs.void"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_19
    java.targetCompatibility = java.sourceCompatibility

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:${findProperty("junitVersion")}")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:${findProperty("junitVersion")}")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = java.sourceCompatibility.toString()
            // https://youtrack.jetbrains.com/issue/KT-4779/Generate-default-methods-for-implementations-in-interfaces
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = java.sourceCompatibility.toString()
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
    }
    if (name != "game") {
        tasks.test {
            maxHeapSize = "4096m"
            useJUnitPlatform()
            failFast = true
            testLogging {
                events("passed", "skipped", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}