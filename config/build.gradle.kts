plugins {
    id("shared")
}

dependencies {
    implementation(libs.kasechange)
    implementation(libs.fastutil)

    testImplementation(libs.mockk)
}
