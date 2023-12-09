package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.plural
import java.io.File
import kotlin.system.measureTimeMillis

private val logger = InlineLogger()

fun loadScripts(scriptPath: String, basePath: String = scriptPath) {
    var scriptCount = 0
    val arguments = emptyArray<String>()
    val directory = File(scriptPath)
    val time = measureTimeMillis {
        for (file in directory.walkTopDown()) {
            if (!file.isFile || file.extension != "kts") {
                continue
            }
            if (loadScript(file, basePath, arguments) != null) {
                scriptCount++
            }
        }
    }
    if (scriptCount == 0) {
        logger.warn { "No scripts found at '${directory.absoluteFile}'" }
    }
    logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${time}ms" }
}

fun loadScript(file: File, basePath: String, arguments: Array<String> = emptyArray()): Any? {
    val start = System.currentTimeMillis()
    try {
        val script = file.path.replace(basePath, "").replace("\\", ".").replace("/", ".").replace(".kts", "")
        val clazz = Class.forName(script)
        val constructors = clazz.constructors
        val constructor = constructors.firstOrNull { it.parameterCount == 1 }
        if (constructor == null) {
            logger.warn { "Unable to find script constructor '$script'." }
            println(constructors.toList())
            return null
        }
        val instance = constructor.newInstance(arguments)
        if (System.currentTimeMillis() - start >= 10) {
            logger.info { "Loaded script ${file.name} in ${System.currentTimeMillis() - start}ms" }
        }
        return instance
    } catch (e: ClassNotFoundException) {
        logger.warn(e) { "Error loading script ${file.name}. Make sure it has a package." }
    }
    return null
}