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
            println("Collect paths")
            val main = sourceSets.getByName("main")
            val outputFile = main.resources.srcDirs.first().resolve("scripts.txt")
            val sourcePaths = main.allSource.srcDirs.first { it.name == "kotlin" }.walkTopDown()
                .filter { it.isFile && it.extension == "kts" }
                .map {
                    it.absolutePath
                        .substringAfter("kotlin${File.separatorChar}")
                        .replace(File.separatorChar, '.')
                        .removeSuffix(".kts")
                }
                .toList()
            outputFile.writeText(sourcePaths.joinToString("\n"))
            println("Collected ${sourcePaths.size} source file paths in ${outputFile.path}")
        }
    }

    named<ShadowJar>("shadowJar") {
        dependsOn("collectSourcePaths")
        from(layout.buildDirectory.file("scripts.txt"))
        minimize {
            exclude(dependency("org.postgresql:postgresql:.*"))
            exclude(dependency("org.jetbrains.exposed:exposed-jdbc:.*"))
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        archiveBaseName.set("void-server-${version}")
        archiveClassifier.set("")
        archiveVersion.set("")
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