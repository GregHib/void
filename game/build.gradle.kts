import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("tasks.metadata")
    id("shared")
    application
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":types"))
    implementation(project(":config"))
    if (findProperty("includeDb") != null) {
        implementation(project(":database"))
    }
    implementation(project(":website"))
    implementation(libs.fastutil)
    implementation(libs.kasechange)
    implementation(libs.rsmod.pathfinder)

    implementation(kotlin("script-runtime"))
    implementation(libs.bundles.kotlinx)

    implementation(libs.koin)
    implementation(libs.bundles.logging)

    testImplementation(libs.bundles.testing)
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
        resourceDirectory = resources
    }

    named<ShadowJar>("shadowJar") {
        dependsOn("scriptMetadata")
        from(layout.buildDirectory.file("scripts.txt"))
        val db = findProperty("includeDb") != null
        minimize {
            if (db) {
                exclude(project(":database"))
                exclude(dependency("org.postgresql:postgresql:.*"))
                exclude(dependency("org.jetbrains.exposed:exposed-jdbc:.*"))
            }
            exclude("world/gregs/voidps/engine/log/**")
            exclude(dependency("ch.qos.logback:logback-classic:.*"))
        }
        if (db) {
            archiveBaseName.set("void-server-db-$version")
        } else {
            archiveBaseName.set("void-server-$version")
        }
        archiveClassifier.set("")
        archiveVersion.set("")
        // Replace logback file as the custom colour classes can't be individually excluded from minimization
        // https://github.com/GradleUp/shadow/issues/638
        exclude("logback.xml")
        val resourcesDir = layout.projectDirectory.dir("src/main/resources")
        val logback =
            resourcesDir
                .file("logback.xml")
                .asFile
                .readText()
                .replace("%colour", "%highlight")
                .replace("%message(%msg){}", "%msg")
        val replacement =
            layout.buildDirectory
                .file("logback-test.xml")
                .get()
                .asFile
        replacement.parentFile.mkdirs()
        replacement.writeText(logback)
        from(replacement)
    }

    register("printVersion") {
        doLast {
            println(project.version)
        }
    }

    register("printCacheVersion") {
        doLast {
            println(libs.versions.cacheVersion.get())
        }
    }

    test {
        jvmArgs("-XX:-OmitStackTraceInFastThrow")
    }
}

distributions {
    create("bundle") {
        distributionBaseName = "void"
        contents {
            from(tasks["shadowJar"])

            val emptyDirs = setOf("cache", "saves")
            val configs =
                parent!!
                    .rootDir
                    .resolve("data")
                    .list()!!
                    .toMutableList()
            configs.removeAll(emptyDirs)
            for (config in configs) {
                from("../data/$config/") {
                    into("data/$config")
                }
            }
            for (dir in emptyDirs) {
                val file =
                    layout.buildDirectory
                        .get()
                        .dir("tmp/empty/$dir/")
                        .asFile
                file.mkdirs()
            }
            from(layout.buildDirectory.dir("tmp/empty/")) {
                into("data")
            }
            val tempDir =
                layout.buildDirectory
                    .dir("tmp/scripts")
                    .get()
                    .asFile
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
            println("Bundling $tempShell")
            from(tempShell)
        }
    }
}

dependencies {
    allprojects.filter { it.name != "tools" }.forEach {
        jacocoAggregation(it)
    }
}

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        targetExclude("temp/", "**/build/**", "**/out/**")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "intellij_idea",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_package-name" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
    flexmark {
        target("**/*.md")
        flexmark()
    }
}
