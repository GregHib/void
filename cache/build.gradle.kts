plugins {
    id("shared")
}

dependencies {
    implementation("world.gregs.voidps.buffer:buffer")
    implementation("world.gregs.config:config")
    implementation(project(":types"))
    implementation(libs.displee.cache)
    implementation(libs.koin)

    implementation(libs.lzma)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.fastutil)

    implementation(libs.bundles.logging)

    testImplementation(libs.mockk)
}
