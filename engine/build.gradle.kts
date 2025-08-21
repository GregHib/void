dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))

    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.jetbrains.exposed:exposed-core:${findProperty("exposedVersion")}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${findProperty("exposedVersion")}")
    implementation("org.postgresql:postgresql:${findProperty("postgresqlVersion")}")
    implementation("com.zaxxer:HikariCP:${findProperty("hikariVersion")}")

    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")

    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")

    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")

    testImplementation("org.testcontainers:junit-jupiter:${findProperty("testcontainersVersion")}")
    testImplementation("org.testcontainers:postgresql:${findProperty("testcontainersVersion")}")
    testImplementation(enforcedPlatform("io.zonky.test.postgres:embedded-postgres-binaries-bom:${findProperty("postgresVersion")}"))
    testImplementation("io.zonky.test:embedded-postgres:${findProperty("embeddedPostgresVersion")}")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.18")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
}

tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}
