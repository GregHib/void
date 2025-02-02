import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.22"))
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

plugins {
    kotlin("jvm") version "1.9.22"
    id("jacoco-report-aggregation")
}

val cacheVersion = "1.3.1"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    group = "world.gregs.void"
    version = System.getenv("GITHUB_REF_NAME") ?: "dev"

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
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility", "-Xallow-any-scripts-in-source-roots")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = java.sourceCompatibility.toString()
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility", "-Xallow-any-scripts-in-source-roots")
        }
    }

    if (name != "tools") {
        tasks.test {
            maxHeapSize = "4096m"
            useJUnitPlatform()
            failFast = true
            testLogging {
                events("passed", "skipped", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
            finalizedBy(tasks.jacocoTestReport)
        }

        tasks.jacocoTestReport {
            dependsOn(tasks.test)
            reports {
                xml.required = true
                csv.required = false
            }
        }
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

tasks.register("printCacheVersion") {
    doLast {
        println(cacheVersion)
    }
}


reporting {
    reports {
        @Suppress("UnstableApiUsage")
        create("jacocoMergedReport", JacocoCoverageReport::class) {
            testType = TestSuiteType.UNIT_TEST
        }
    }
}

dependencies {
    allprojects.filter { it.name != "tools" }.forEach {
        jacocoAggregation(it)
    }
}