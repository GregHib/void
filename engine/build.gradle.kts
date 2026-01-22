plugins {
    id("shared")
}

dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation("world.gregs.config:config")

    implementation(libs.fastutil)
    implementation(libs.kasechange)

    implementation(libs.koin)
    implementation(libs.rsmod.pathfinder)

    implementation(libs.bundles.logging)
    implementation(libs.bundles.kotlinx)
    testImplementation(libs.bundles.testing)
}

tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}
