plugins {
    id("shared")
}

dependencies {
    implementation(project(":engine"))
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
    implementation(libs.exposed)
    implementation(libs.exposed.jdbc)
    implementation("com.h2database:h2:2.4.240")
}
