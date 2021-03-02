plugins {
    `java-library`
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":utility"))
    implementation(project(":ai"))
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("io.github.classgraph:classgraph:${findProperty("classgraphVersion")}")
    implementation("io.netty:netty-all:${findProperty("nettyVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${findProperty("jacksonVersion")}")

    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.koin:koin-core:${findProperty("koinVersion")}")

    implementation("org.koin:koin-logger-slf4j:${findProperty("koinVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("org.koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")

}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}