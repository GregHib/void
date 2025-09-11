import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("kotlin")
    id("idea")
    id("jacoco")
    id("com.diffplug.spotless")
    id("jacoco-report-aggregation")
}

group = "world.gregs.void"
version = System.getenv("GITHUB_REF_NAME") ?: "dev"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = java.sourceCompatibility

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
//         https://youtrack.jetbrains.com/issue/KT-4779/Generate-default-methods-for-implementations-in-interfaces
        freeCompilerArgs.addAll("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.platform:junit-platform-launcher:1.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.4")
}

if (name != "tools") {
    tasks.test {
        maxHeapSize = "5120m"
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
