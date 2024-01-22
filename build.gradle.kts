buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.21"))
    }
}

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "world.gregs.void"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_19

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
            kotlinOptions.jvmTarget = JavaVersion.VERSION_19.toString()
            // https://youtrack.jetbrains.com/issue/KT-4779/Generate-default-methods-for-implementations-in-interfaces
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_19.toString()
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
        test {
            useJUnitPlatform()
            failFast = true
        }
    }

}

kover {
    useJacoco()
}

koverReport {
    filters {
        includes {
            classes("world.gregs.voidps")
        }
    }
}