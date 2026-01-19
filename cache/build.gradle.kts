plugins {
    id("shared")
}

dependencies {
    implementation(project(":buffer"))
    implementation(project(":types"))
    implementation(libs.displee.cache)
    implementation(libs.koin)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0-RC")

    implementation(libs.lzma)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.fastutil)

    implementation(libs.bundles.logging)

    testImplementation(libs.mockk)
}
