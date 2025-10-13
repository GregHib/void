plugins {
    id("shared")
}

dependencies {
    implementation(project(":types"))
    implementation(project(":engine"))
    implementation(libs.bundles.database)
    implementation(libs.bundles.logging)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.postgres)
    testImplementation(enforcedPlatform(libs.embedded.postgres.binaries))
}
