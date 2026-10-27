plugins {
    id("shared")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.junit.params)
}
