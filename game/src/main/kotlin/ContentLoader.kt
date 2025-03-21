import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.Settings
import java.io.File
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed list made by gradle build task
 */
object ContentLoader {
    private val logger = InlineLogger()
    private val arguments = emptyArray<String>()

    fun load() {
        val start = System.currentTimeMillis()
        var scriptCount =  0
        val scripts = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader()
        if (scripts == null) {
            logger.debug { "No auto-generated script file found, falling back to manual search." }
            for (script in loadScriptPaths(Settings["storage.scripts.path"])) {
                loadScript(script)
                scriptCount++
            }
        } else {
            var script = ""
            try {
                while (scripts.ready()) {
                    script = scripts.readLine()
                    loadScript(script)
                    scriptCount++
                }
                scripts.close()
            } catch (e: Exception) {
                scripts.close()
                logger.error(e) { "Failed to load script: $script" }
                logger.error { "If the file exists make sure the scripts package is correct." }
                logger.error { "If the file has been deleted make sure 'gradle collectSourcePaths' is correctly running." }
                exitProcess(1)
            }
        }

        if (scriptCount == 0) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadScript(name: String) {
        val clazz = Class.forName(name)
        val constructor = clazz.declaredConstructors.first()
        constructor.newInstance(arguments)
    }

    private fun loadScriptPaths(sourcePath: String): List<String> {
        return File(sourcePath)
            .walkTopDown()
            .filter { it.isFile && it.extension == "kts" }
            .map {
                it.path
                    .substringAfter("kotlin${File.separator}")
                    .replace(File.separator, ".")
                    .removeSuffix(".kts")
            }
            .toList()
    }
}