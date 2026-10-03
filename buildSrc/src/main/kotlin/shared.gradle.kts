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
        freeCompilerArgs.addAll("-Xinline-classes", "-Xcontext-parameters", "-Xjvm-default=all-compatibility")
    }
}

spotless {
    // Nested modules (e.g. tools:app inside tools) check their own files
    val nested = subprojects.map { "${it.projectDir.relativeTo(projectDir).invariantSeparatorsPath}/**" }
    kotlin {
        target("**/*.kt", "**/*.kts")
        targetExclude(listOf("temp/**", "**/build/**", "**/out/**") + nested)
        val rules = mutableMapOf<String, Any>(
            "ktlint_code_style" to "intellij_idea",
            "ktlint_standard_no-wildcard-imports" to "disabled",
            "ktlint_standard_package-name" to "disabled",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        )
        if (path == ":tools:render") {
            // Ported client renderer which keeps its original obfuscated names
            rules["ktlint_standard_property-naming"] = "disabled"
            rules["ktlint_standard_function-naming"] = "disabled"
            rules["ktlint_standard_class-naming"] = "disabled"
        }
        ktlint().editorConfigOverride(rules)
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
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
