dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("io.ktor:ktor-server-core-jvm:${findProperty("ktorVersion")}")
    implementation("io.ktor:ktor-network-jvm:${findProperty("ktorVersion")}")
    implementation("org.mindrot:jbcrypt:${findProperty("jbcryptVersion")}")

    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
}
