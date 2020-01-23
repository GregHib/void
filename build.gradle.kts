plugins {
    kotlin("jvm") version "1.3.61"
}

buildscript {
    repositories {
        jcenter()
    }
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "redrune"
    version = "0.0.1"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://repo.maven.apache.org/maven2")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        //Main
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.netty:netty-all:4.1.44.Final")
        compile(group = "org.yaml", name = "snakeyaml", version = "1.8")
        compile(group = "io.github.classgraph", name = "classgraph", version = "4.6.3")

        //Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")

        //Utilities
        implementation("com.google.guava:guava:19.0")
        implementation("org.apache.commons:commons-lang3:3.0")
//        implementation("io.github.classgraph:classgraph:4.8.60")

        //Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    }

/*
    idea {
        module {
            inheritOutputDirs = false
            outputDir = file("${project.buildDir}/classes/kotlin/main")
            testOutputDir = file("${project.buildDir}/classes/kotlin/test")
        }
    }*/

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

}
