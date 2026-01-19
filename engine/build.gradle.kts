plugins {
    id("shared")
}

dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))

    implementation(libs.fastutil)
    implementation(libs.kasechange)

    implementation(libs.koin)
    implementation(libs.rsmod.pathfinder)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.10.0-RC")
    implementation(libs.bundles.logging)
    implementation(libs.bundles.kotlinx)
    testImplementation(libs.bundles.testing)
}

tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}
