dependencies {
    implementation(project(":types"))
    implementation(project(":engine"))
    implementation(project(":network"))
    implementation(project(":cache"))
    implementation(project(":yaml"))
    implementation(project(":game:api"))
    implementation(project(":game:bot"))
    implementation(kotlin("script-runtime"))
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")
    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")

    implementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    implementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    implementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")
}