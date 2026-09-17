plugins {
    id("shared")
}

dependencies {
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
}
