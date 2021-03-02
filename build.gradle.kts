
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

val koinVersion = "2.2.2"
val junitVersion = findProperty("junitVersion")
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
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:${findProperty("junitVersion")}")
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
