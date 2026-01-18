import com.github.michaelbull.logging.InlineLogger
import content.bot.Bots
import content.skill.prayer.PrayerApi
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.get
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed scripts.txt list made by scriptMetadata gradle build task
 */
class ContentLoader {
    private val logger = InlineLogger()

    fun load(): List<Script> {
        val start = System.currentTimeMillis()
        val scriptNames = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader() ?: error("No auto-generated script file found, make sure 'gradle scriptMetadata' is correctly running")
        loadContentApis()
        val scripts = mutableListOf<Script>()
        var script = ""
        try {
            while (scriptNames.ready()) {
                script = scriptNames.readLine()
                val name = script.substringAfterLast("|")
                scripts.add(loadScript(name) as Script)
            }
            scriptNames.close()
        } catch (e: Exception) {
            scriptNames.close()
            logger.error(e) { "Failed to load script: $script" }
            logger.error { "If the file exists make sure the scripts package is correct." }
            logger.error { "If the file has been deleted try running 'gradle cleanScriptMetadata'." }
            logger.error { "Otherwise make sure the return type is written explicitly." }
            exitProcess(1)
        }
        if (scripts.isEmpty()) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded ${scripts.size} ${"script".plural(scripts.size)} in ${System.currentTimeMillis() - start}ms" }
        return scripts
    }

    private fun loadContentApis() {
        Script.interfaces.add(PrayerApi)
        Script.interfaces.add(Bots)
    }

    private fun loadScript(name: String): Any {
        val clazz = Class.forName(name)
        val constructor = clazz.declaredConstructors.first()
        val params = constructor.parameters.map { get(it.type.kotlin) }.toTypedArray()
        return constructor.newInstance(*params)
    }
}
