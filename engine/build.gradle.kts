import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":utility"))
    implementation("io.github.classgraph:classgraph:4.8.65")
    implementation(kotlin("script-runtime"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}