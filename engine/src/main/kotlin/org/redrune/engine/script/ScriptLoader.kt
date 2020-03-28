package org.redrune.engine.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import org.redrune.utility.func.plural
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class ScriptLoader {

    private val logger = InlineLogger()

    init {
        var scripts = 0
        val time = measureTimeMillis {
            val arguments = emptyArray<String>()
            ClassGraph()
                .enableClassInfo()
                .filterClasspathElements { it.contains("game") && !it.contains("tmp") }
                .scan().use { scanResult ->
                    for (script in scanResult.allClasses.filter { it.extendsSuperclass("kotlin.script.templates.standard.ScriptTemplateWithArgs") }) {
                        val klass = script.loadClass()
                        klass.constructors.first().newInstance(arguments)
                        scripts++
                    }
                }
        }

        logger.info { "$scripts ${"script".plural(scripts)} loaded in ${time}ms" }
    }
}