dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":utility"))

    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("io.ktor:ktor-server-core:${findProperty("ktorVersion")}")
    implementation("io.ktor:ktor-network:${findProperty("ktorVersion")}")
    implementation("io.netty:netty-all:${findProperty("nettyVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")

    implementation("com.fasterxml.jackson.core:jackson-core:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")

    implementation("org.koin:koin-core:${findProperty("koinVersion")}")

    implementation("org.koin:koin-logger-slf4j:${findProperty("koinVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("org.koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}