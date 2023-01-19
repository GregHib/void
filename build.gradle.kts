buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.0"))
    }
}

plugins {
    kotlin("jvm") version "1.7.0"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "world.gregs.void"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_18

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:${findProperty("junitVersion")}")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
        }
    }

}
