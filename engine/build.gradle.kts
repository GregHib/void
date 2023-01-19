dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":network"))

    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")

    implementation("org.yaml:snakeyaml:${findProperty("snakeYamlVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-core:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")

    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("org.mindrot:jbcrypt:${findProperty("jbcryptVersion")}")
    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")

    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}