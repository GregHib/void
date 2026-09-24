plugins {
    id("shared")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":types"))
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
    implementation(libs.bundles.ktor.api)
    implementation(libs.koin)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlinx.coroutines.test)
}
