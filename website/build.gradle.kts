plugins {
    id("shared")
    kotlin("plugin.serialization") version "2.2.20"
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))
    if (findProperty("includeDb") != null) {
        implementation(project(":database"))
    }

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.resources)
    implementation(libs.logback)

    implementation(libs.koin)
    implementation(libs.jbcrypt)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.kotlinx)
    implementation("io.ktor:ktor-server-sessions-jvm:3.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}
