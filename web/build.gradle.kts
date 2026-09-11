plugins {
    id("shared")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
    implementation(libs.bundles.ktor.api)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlinx.coroutines.test)
}
