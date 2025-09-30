import com.github.michaelbull.logging.InlineLogger
import content.skill.thieving.Stole
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.dispatch.Dispatcher
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.skill.level.LevelChanged
import world.gregs.voidps.engine.get
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed scripts.txt list made by scriptMetadata gradle build task
 * Script instances are passed to their relevant [Dispatcher] when they contain an overridden method (also listed in the precomputed list)
 */
object ContentLoader {
    private val logger = InlineLogger()

    private val dispatchers = mutableMapOf<String, Dispatcher<*>>(
        method("spawn", "Player") to Spawn.playerDispatcher,
        method("spawn", "NPC") to Spawn.npcDispatcher,
        method("spawn", "FloorItem") to Spawn.floorItemDispatcher,
        method("spawn", "GameObject") to Spawn.objectDispatcher,
        method("worldSpawn") to Spawn.worldDispatcher,
        method("worldSpawn", "ConfigFiles") to Spawn.worldDispatcher,
        method("levelChanged", "NPC", "Skill", "Int", "Int") to LevelChanged.npcDispatcher,
        method("levelChanged", "Player", "Skill", "Int", "Int") to LevelChanged.playerDispatcher,
        method("move", "Player", "Tile", "Tile") to Moved.playerDispatcher,
        method("move", "NPC", "Tile", "Tile") to Moved.npcDispatcher,
        method("variableSet", "Player", "String", "Any?", "Any?") to VariableSet.playerDispatcher,
        method("variableSet", "NPC", "String", "Any?", "Any?") to VariableSet.npcDispatcher,
        method("stole", "Player", "GameObject", "Item") to Stole.dispatcher,
    )

    fun load() {
        val start = System.currentTimeMillis()
        val scripts = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader() ?: error("No auto-generated script file found, make sure 'gradle scriptMetadata' is correctly running")
        var scriptCount = 0
        var script = ""
        try {
            val instances = mutableMapOf<String, Any>()
            while (scripts.ready()) {
                script = scripts.readLine()
                val name = script.substringAfterLast("|")
                val instance = instances.getOrPut(name) { loadScript(name) }
                if (name.length == script.length) {
                    continue
                }
                val parts = script.split("|")
                val method = parts[parts.lastIndex - 1]
                val dispatcher = dispatchers[method] ?: error("Unknown dispatcher for method: $method. Make sure it's registered in ContentLoader.kt")
                if (script[0] == '@' && parts.size == 4) {
                    dispatcher.load(instance, parts[0], parts[1])
                } else {
                    dispatcher.load(instance, "", "*")
                }
            }
            scriptCount = instances.size
            scripts.close()
        } catch (e: Exception) {
            scripts.close()
            logger.error(e) { "Failed to load script: $script" }
            logger.error { "If the file exists make sure the scripts package is correct." }
            logger.error { "If the file has been deleted try running 'gradle cleanScriptMetadata'." }
            exitProcess(1)
        }

        if (scriptCount == 0) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${System.currentTimeMillis() - start}ms" }
    }

    private fun method(name: String, vararg argTypes: String, returnType: Any = Unit, annotation: String? = null) = buildString {
        if (annotation != null) {
            append(annotation).append("@")
        }
        append(name)
        append("(").append(argTypes.joinToString(",")).append(")")
        if (returnType != Unit) {
            append(":").append(returnType::class.simpleName)
        }
    }

    fun clear() {
        for (dispatcher in dispatchers.values) {
            dispatcher.clear()
        }
    }

    private fun loadScript(name: String): Any {
        val clazz = Class.forName(name)
        val constructor = clazz.declaredConstructors.first()
        val params = constructor.parameters.map { get(it.type.kotlin) }.toTypedArray()
        return constructor.newInstance(*params)
    }
}
