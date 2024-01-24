package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import world.gregs.voidps.Main
import world.gregs.voidps.engine.client.ui.chat.plural
import java.io.File
import kotlin.system.measureTimeMillis

private val logger = InlineLogger()

fun loadScripts(scriptPath: String) {
    var scriptCount = 0
    val found = mutableSetOf<String>()
    val isJar = Main::class.java.getResource("Main.class")?.protocol == "jar"
    val arguments = emptyArray<String>()
    val time = measureTimeMillis {
        ClassGraph()
            .filterClasspathElements { isJar || !it.endsWith(".jar") }
            .acceptPackages("world.gregs.voidps.world", "world.gregs.voidps.bot")
            .enableMethodInfo()
            .scan().use { scanResult ->
                for (info in scanResult.allClasses) {
                    if (!info.hasMethod("main")) {
                        continue
                    }
                    val name = info.name
                    found.add(name)
                    scriptCount++
                    val clazz = Class.forName(name)
                    val constructor = clazz.declaredConstructors.first()
                    constructor.newInstance(arguments)
                }
            }
    }
    val directory = File(scriptPath)
    if (scriptCount == 0) {
        logger.warn { "No scripts found at '${directory.absoluteFile}'" }
    }
    logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${time}ms" }
}