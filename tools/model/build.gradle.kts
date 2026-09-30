plugins {
    id("shared")
}

kotlin {
    dependencies {
        implementation(project(":buffer"))
        implementation(project(":cache"))
        implementation(project(":config"))
        implementation(project(":engine"))
        implementation(project(":game"))
        implementation(project(":types"))
        implementation(libs.fastutil)
        implementation(libs.koin)
    }
}

tasks.register<JavaExec>("dumpQuickChat") {
    group = "bot chat"
    description = "Prints every quick chat phrase with its enum options."
    mainClass.set("world.gregs.voidps.tools.model.QuickChatEnumDump")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
}

tasks.register<JavaExec>("evaluateBotChat") {
    group = "bot chat"
    description = "Reports bot chat intent accuracy on held-out examples."
    mainClass.set("world.gregs.voidps.tools.model.EvaluateBotChat")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
}

tasks.register<JavaExec>("botChatConsole") {
    group = "bot chat"
    description = "Classify typed messages with the saved bot chat model."
    mainClass.set("world.gregs.voidps.tools.model.BotChatConsole")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
    standardInput = System.`in`
}

