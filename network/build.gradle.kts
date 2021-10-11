dependencies {
    implementation(project(":buffer"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("io.ktor:ktor-server-core:${findProperty("ktorVersion")}")
    implementation("io.ktor:ktor-network:${findProperty("ktorVersion")}")
}