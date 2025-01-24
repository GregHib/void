import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":network"))
    api(project(":game"))
    implementation(project(":yaml"))
    implementation(project(":types"))

    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")
    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")

    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

}

application {
    mainClass.set("world.gregs.voidps.Main")
}

tasks {
    named<ShadowJar>("shadowJar") {
        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-script-runtime:.*"))
            exclude(dependency("org.postgresql:postgresql:.*"))
            exclude(dependency("org.jetbrains.exposed:exposed-jdbc:.*"))
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        archiveBaseName.set("void-server-${version}")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}

distributions {
    create("bundle") {
        distributionBaseName = "void"
        contents {
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