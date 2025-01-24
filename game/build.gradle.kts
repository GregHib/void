dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":yaml"))
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")

    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")
    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("io.github.classgraph:classgraph:${findProperty("classgraphVersion")}")

    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")
}

val commonDeps = project.configurations.getByName("implementation").dependencies

subprojects {
    dependencies {
        for(dep in commonDeps) {
            implementation(dep)
        }
    }
}

dependencies {
    for (project in subprojects) {
        api(project)
    }
}