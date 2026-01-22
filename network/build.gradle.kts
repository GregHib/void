plugins {
    id("shared")
}

dependencies {
    implementation("world.gregs.voidps.buffer:buffer")
    implementation(project(":cache"))

    implementation(libs.ktor)
    implementation(libs.ktor.network)
    implementation(libs.jbcrypt)
    implementation(libs.inline.logging)
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.bundles.testing)
}
