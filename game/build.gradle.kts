import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
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
    implementation("io.github.classgraph:classgraph:4.8.165")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")

}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}

application {
    mainClass.set("world.gregs.voidps.Main")
    tasks.run.get().workingDir = rootProject.projectDir
}

tasks {
    val name = "void-server-${version}"
    named<ShadowJar>("shadowJar") {
        minimize {
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        archiveBaseName.set(name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }
    register("buildScripts") {
        var file = layout.buildDirectory.get().file("scripts/run-server.bat").asFile
        file.parentFile.mkdirs()
        file.writeText("""
            @echo off
            title Void Game Sever
            java -jar $name.jar
            pause
        """.trimIndent())
        file = layout.buildDirectory.get().file("scripts/run-server.sh").asFile
        file.writeText("""
            #!/usr/bin/env bash
            title="Void Game Server"
            echo -e '\033]2;'${'$'}title'\007'
            # Early exit on cancel
            cleanup() {
            	echo ""
                exit 1
            }
            trap cleanup INT
            java -jar $name.jar
            # Stop console closing
            if [ ${'$'}? -ne 0 ]; then
                echo "Error: The Java application exited with a non-zero status."
                read -p "Press enter to continue..."
            fi 
        """.trimIndent())
    }
}

distributions {
    create("bundle") {
        distributionBaseName = "void"
        contents {
            from(tasks["shadowJar"])
            from(tasks["buildScripts"])
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
                val file = layout.buildDirectory.get().dir("/tmp/$dir/").asFile
                file.mkdirs()
            }
            from(layout.buildDirectory.dir("tmp/")) {
                into("data")
            }
            from(layout.projectDirectory.dir("src/main/resources/game.properties"))
            from(layout.buildDirectory.dir("scripts/run-server.bat"))
            from(layout.buildDirectory.dir("scripts/run-server.sh"))
        }
    }
}