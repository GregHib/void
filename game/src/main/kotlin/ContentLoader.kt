import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.get
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed list made by gradle build task
 */
object ContentLoader {
    private val logger = InlineLogger()

    fun load() {
        val start = System.currentTimeMillis()
        var scriptCount = 0
        val scripts = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader()
        if (scripts == null) {
            error("No auto-generated script file found, make sure 'gradle scriptMetadata' is correctly running")
        }
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
            logger.error { "If the file has been deleted make sure 'gradle scriptMetadata' is correctly running." }
            exitProcess(1)
        }

        if (scriptCount == 0) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadScript(name: String) {
        val clazz = Class.forName(name)
        val constructor = clazz.declaredConstructors.first()
        val params = constructor.parameters.map { get(it.type.kotlin) }.toTypedArray()
        constructor.newInstance(*params)
    }

}
