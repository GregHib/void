package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import world.gregs.voidps.engine.client.ui.chat.plural
import kotlin.system.measureTimeMillis

fun loadScripts(scriptModule: String) {
    val logger = InlineLogger()
    var scripts = 0
    val time = measureTimeMillis {
        val arguments = emptyArray<String>()
        ClassGraph()
            .enableClassInfo()
            .filterClasspathElements { path -> path.contains(scriptModule) && !path.contains("tmp") }
            .scan().use { scanResult ->
                for (script in scanResult.allClasses.filter { it.extendsSuperclass("kotlin.script.templates.standard.ScriptTemplateWithArgs") }) {
                    val klass = script.loadClass()
                    klass.constructors.first().newInstance(arguments)
                    scripts++
                }
            }
    }

    logger.info { "$scripts $scriptModule ${"script".plural(scripts)} loaded in ${time}ms" }
}