plugins {
    id("shared")
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktor.server.test.host)
}
