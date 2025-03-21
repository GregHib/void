import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":yaml"))
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

    named("build") {
        dependsOn("collectSourcePaths")
    }

    named("classes") {
        dependsOn("collectSourcePaths")
    }

    register("collectSourcePaths") {
        doLast {
            val start = System.nanoTime()
            val main = sourceSets.getByName("main")
            val outputFile = main.resources.srcDirs.first { it.name == "resources" }.resolve("scripts.txt")
            var count = 0
            outputFile.writer().buffered().use { output ->
                for (file in main.allSource.srcDirs.first { it.name == "kotlin" }.walkTopDown()) {
                    if (file.extension == "kts") {
                        output.write(
                            file.absolutePath
                                .substringAfter("kotlin${File.separatorChar}")
                                .replace(File.separatorChar, '.')
                                .removeSuffix(".kts")
                        )
                        output.write('\n'.code)
                        count++
                    }
                }
            }
            println("Collected $count source file paths to ${outputFile.path} in ${System.nanoTime() - start} ms")
        }
    }

    named<ShadowJar>("shadowJar") {
        dependsOn("collectSourcePaths")
        from(layout.buildDirectory.file("scripts.txt"))
        minimize {
            exclude("world/gregs/voidps/engine/log/**")
            exclude(dependency("org.postgresql:postgresql:.*"))
            exclude(dependency("org.jetbrains.exposed:exposed-jdbc:.*"))
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        archiveBaseName.set("void-server-${version}")
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
            from(tasks["collectSourcePaths"])
            from(tasks["shadowJar"])
            from("../data/definitions/") {
                into("data/definitions")
            }
            from("../data/map") {
                into("data/map")
            }
            from("../data/spawns") {
                into("data/spawns")
            }
            val emptyDirs = listOf("cache", "saves")
            for (dir in emptyDirs) {
                val file = layout.buildDirectory.get().dir("tmp/empty/$dir/").asFile
                file.mkdirs()
            }
            from(layout.buildDirectory.dir("tmp/empty/")) {
                into("data")
            }
            val resourcesDir = layout.projectDirectory.dir("src/main/resources")
            from(resourcesDir.file("game.properties"))
            val bat = resourcesDir.file("run-server.bat").asFile
            bat.writeText(bat.readText().replace("-dev.jar", "-${version}.jar"))
            from(bat)
            val shell = resourcesDir.file("run-server.sh").asFile
            shell.writeText(shell.readText().replace("-dev.jar", "-${version}.jar"))
            from(shell)
        }
    }
}