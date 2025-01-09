package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import world.gregs.voidps.Main
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.Settings
import kotlin.system.measureTimeMillis

private val logger = InlineLogger("ScriptLoader")

fun loadScripts(scriptPackage: String = Settings["scriptPackage"], botScriptPackage: String = Settings["botScriptPackage"]) {
    var scriptCount = 0
    val found = mutableSetOf<String>()
    val isJar = Main::class.java.getResource("${Main::class.simpleName}.class")?.protocol == "jar"
    val arguments = emptyArray<String>()
    val time = measureTimeMillis {
        ClassGraph()
            .filterClasspathElements { isJar || !it.endsWith(".jar") }
            .acceptPackages(scriptPackage, botScriptPackage)
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
    if (scriptCount == 0) {
        logger.warn { "No scripts found." }
    }
    logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${time}ms" }
}