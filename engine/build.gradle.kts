dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))

    implementation(kotlin("reflect"))

    implementation(libs.bundles.database)

    implementation(libs.fastutil)
    implementation(libs.kasechange)

    implementation(libs.koin)
    implementation(libs.rsmod.pathfinder)

    implementation(libs.bundles.logging)
    implementation(libs.bundles.kotlinx)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.postgres)
    testImplementation(enforcedPlatform(libs.embedded.postgres.binaries))
}

tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}
