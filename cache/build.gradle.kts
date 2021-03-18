dependencies {
    implementation(project(":buffer"))
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("org.koin:koin-core:${findProperty("koinVersion")}")

    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}