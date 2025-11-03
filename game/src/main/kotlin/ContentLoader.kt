import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.get
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed scripts.txt list made by scriptMetadata gradle build task
 */
object ContentLoader {
    private val logger = InlineLogger()

    fun load() {
        var start = System.currentTimeMillis()
        val matches = ContentLoader::class.java.getResourceAsStream("matches.txt")?.bufferedReader() ?: error("No auto-generated matches file found, make sure 'gradle scriptMetadata' is correctly running")
        Wildcards.load(matches)
        matches.close()
        logger.info { "Loaded ${Wildcards.size} ${"wildcard".plural(Wildcards.size)} in ${System.currentTimeMillis() - start}ms" }
        start = System.currentTimeMillis()
        val scripts = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader() ?: error("No auto-generated script file found, make sure 'gradle scriptMetadata' is correctly running")
        var scriptCount = 0
        var script = ""
        try {
            while (scripts.ready()) {
                script = scripts.readLine()
                val name = script.substringAfterLast("|")
                loadScript(name)
                scriptCount++
            }
            scripts.close()
        } catch (e: Exception) {
            scripts.close()
            logger.error(e) { "Failed to load script: $script" }
            logger.error { "If the file exists make sure the scripts package is correct." }
            logger.error { "If the file has been deleted try running 'gradle cleanScriptMetadata'." }
            logger.error { "Otherwise make sure the return type is written explicitly." }
            exitProcess(1)
        }
        if (scriptCount == 0) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${System.currentTimeMillis() - start}ms" }
    }

    fun clear() {
        Script.clear()
    }

    private fun loadScript(name: String): Any {
        val clazz = Class.forName(name)
        val constructor = clazz.declaredConstructors.first()
        val params = constructor.parameters.map { get(it.type.kotlin) }.toTypedArray()
        return constructor.newInstance(*params)
    }
}
