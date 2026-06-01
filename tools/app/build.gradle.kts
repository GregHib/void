import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("shared")
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

version = "1.0.1"

kotlin {
    jvmToolchain(21)

    dependencies {
        implementation(kotlin("reflect"))
        implementation(project(":buffer"))
        implementation(project(":cache"))
        implementation(project(":engine"))
        implementation(project(":config"))
        implementation(project(":types"))

        implementation(libs.bundles.compose)
//        implementation(compose.desktop.currentOs)
        implementation(libs.bundles.compose.release)

        implementation(libs.kotlinx.io)
        implementation(libs.kotlinx.coroutines)

        implementation("org.apache.commons:commons-compress:1.24.0")
        implementation(libs.kasechange)

        implementation(libs.koin)
        implementation(libs.displee.cache)
        implementation(libs.fastutil)

        testImplementation(libs.mockk)
    }

    sourceSets {
        val main by getting {
            resources.srcDir(file("composeResources/drawable"))
        }
    }
}

tasks {
    withType<AbstractTestTask>().configureEach {
        failOnNoDiscoveredTests = false
    }

    withType<JavaExec> {
        workingDir = projectDir
    }

    processResources {
        from("../../game/src/main/resources/") {
            include("game.properties")
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "world.gregs.voidps.tools.search.AppKt"
            nativeDistributions {
                targetFormats(TargetFormat.Msi, TargetFormat.Deb)
                packageName = "void-cache-viewer"
                packageVersion = version.toString()
                val icon = project.file("src/main/composeResources/drawable/void_icon.png")
                windows {
                    includeAllModules = true
                    iconFile.set(icon)
                }
                linux {
                    iconFile.set(icon)
                }
            }

            buildTypes.release.proguard {
                isEnabled.set(true)
                optimize.set(true)
                configurationFiles.from(project.file("compose-proguard-rules.pro"))
            }
        }
    }
}