plugins {
    id("shared")
}

dependencies {
    implementation(project(":config"))
    implementation(project(":buffer"))
    implementation(project(":types"))
    implementation(libs.displee.cache)
    implementation(libs.koin)

    implementation(libs.lzma)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.fastutil)

    implementation(libs.bundles.logging)

    testImplementation(libs.mockk)
}
