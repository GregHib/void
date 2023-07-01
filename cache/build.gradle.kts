dependencies {
    implementation(project(":buffer"))
    implementation(project(":types"))
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")

    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}