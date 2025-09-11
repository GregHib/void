import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    dependencies {
        classpath(libs.kotlin)
        classpath(libs.shadow)
    }
}

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.jacoco.aggregation)
    alias(libs.plugins.spotless)
}

val cacheVersion = "1.3.1"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    group = "world.gregs.void"
    version = System.getenv("GITHUB_REF_NAME") ?: "dev"

    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = java.sourceCompatibility

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            // https://youtrack.jetbrains.com/issue/KT-4779/Generate-default-methods-for-implementations-in-interfaces
            freeCompilerArgs.addAll("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
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
}

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        targetExclude("temp/", "**/build/**", "**/out/**")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "intellij_idea",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_package-name" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
    flexmark {
        target("**/*.md")
        flexmark()
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

dependencies {
    allprojects.filter { it.name != "tools" }.forEach {
        jacocoAggregation(it)
    }
}
