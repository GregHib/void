import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")

    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")
    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")

    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")
}

application {
    mainClass.set("Main")
    tasks.run.get().workingDir = rootProject.projectDir
}

tasks {

    processResources {
        dependsOn("scriptMetadata")
    }

    named("build") {
        dependsOn("scriptMetadata")
    }

    named("classes") {
        dependsOn("scriptMetadata")
    }

    register("scriptMetadata", ScriptMetadataTask::class.java) {
        val main = sourceSets.getByName("main")
        val resources = main.resources.srcDirs.first { it.name == "resources" }
        inputDirectory.set(layout.projectDirectory.dir("src/main/kotlin/content"))
        scriptsFile = resources.resolve("scripts.txt")
    }

    named<ShadowJar>("shadowJar") {
        dependsOn("scriptMetadata")
        from(layout.buildDirectory.file("scripts.txt"))
        minimize {
            exclude("world/gregs/voidps/engine/log/**")
            exclude(dependency("org.postgresql:postgresql:.*"))
            exclude(dependency("org.jetbrains.exposed:exposed-jdbc:.*"))
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        archiveBaseName.set("void-server-$version")
        archiveClassifier.set("")
        archiveVersion.set("")
        // Replace logback file as the custom colour classes can't be individually excluded from minimization
        // https://github.com/GradleUp/shadow/issues/638
        exclude("logback.xml")
        val resourcesDir = layout.projectDirectory.dir("src/main/resources")
        val logback = resourcesDir.file("logback.xml").asFile
            .readText()
            .replace("%colour", "%highlight")
            .replace("%message(%msg){}", "%msg")
        val replacement = layout.buildDirectory.file("logback-test.xml").get().asFile
        replacement.parentFile.mkdirs()
        replacement.writeText(logback)
        from(replacement)
    }

    withType<Test> {
        jvmArgs("-XX:-OmitStackTraceInFastThrow")
    }
}

distributions {
    create("bundle") {
        distributionBaseName = "void"
        contents {
            from(tasks["shadowJar"])

            val emptyDirs = setOf("cache", "saves")
            val configs = parent!!.rootDir.resolve("data").list()!!.toMutableList()
            configs.removeAll(emptyDirs)
            for (config in configs) {
                from("../data/$config/") {
                    into("data/$config")
                }
            }
            for (dir in emptyDirs) {
                val file = layout.buildDirectory.get().dir("tmp/empty/$dir/").asFile
                file.mkdirs()
            }
            from(layout.buildDirectory.dir("tmp/empty/")) {
                into("data")
            }
            val tempDir = layout.buildDirectory.dir("tmp/scripts").get().asFile
            tempDir.mkdirs()
            val resourcesDir = layout.projectDirectory.dir("src/main/resources")
            from(resourcesDir.file("game.properties"))
            val bat = resourcesDir.file("run-server.bat").asFile
            val tempBat = File(tempDir, "run-server.bat")
            tempBat.writeText(bat.readText().replace("-dev.jar", "-$version.jar"))
            from(tempBat)
            val shell = resourcesDir.file("run-server.sh").asFile
            val tempShell = File(tempDir, "run-server.sh")
            tempShell.writeText(shell.readText().replace("-dev.jar", "-$version.jar"))
            from(tempShell)
        }
    }
}
