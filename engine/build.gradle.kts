plugins {
    `java-library`
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":utility"))
    implementation("io.github.classgraph:classgraph:4.8.65")
    implementation(kotlin("script-runtime"))
}