plugins {
    id("shared")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":tools:avatar"))
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.network)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.server.html)
    implementation(libs.bundles.ktor.api)
    implementation(libs.koin)
    implementation(libs.jbcrypt)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.kotlinx.coroutines.test)
}
