package world.gregs.voidps.script

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import world.gregs.voidps.utility.func.plural
import kotlin.system.measureTimeMillis

val scriptModule = module {
    single(createdAtStart = true) {
        loadScripts(getProperty("scriptModule"))
    }
}

fun loadScripts(scriptModule: String) = GlobalScope.launch {
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