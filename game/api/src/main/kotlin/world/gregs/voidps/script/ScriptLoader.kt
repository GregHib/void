package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.Settings
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

private val logger = InlineLogger("ScriptLoader")

object ScriptLoader {
    fun load(mainClass: KClass<*>, vararg packages: String) {
        var scriptCount = 0
        val found = mutableSetOf<String>()
        val isJar = mainClass.java.getResource("${mainClass.simpleName}.class")?.protocol == "jar"
        val arguments = emptyArray<String>()
        val time = measureTimeMillis {
            ClassGraph()
                .filterClasspathElements { isJar || !it.endsWith(".jar") }
                .acceptPackages(*packages)
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
}
