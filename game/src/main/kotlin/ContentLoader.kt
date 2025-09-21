import com.github.michaelbull.logging.InlineLogger
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

    val dispatchers = mutableMapOf<String, Dispatcher<*>>(
        "spawn(Player)" to Spawn.playerDispatcher,
        "spawn(NPC)" to Spawn.npcDispatcher,
        "spawn(FloorItem)" to Spawn.floorItemDispatcher,
        "spawn(GameObject)" to Spawn.objectDispatcher,
        "worldSpawn()" to Spawn.worldDispatcher,
        "levelChanged(NPC,Skill,Int,Int)" to LevelChanged.npcDispatcher,
        "levelChanged(Player,Skill,Int,Int)" to LevelChanged.playerDispatcher,
        "move(Player,Tile,Tile)" to Moved.playerDispatcher,
        "move(NPC,Tile,Tile)" to Moved.npcDispatcher,
        "variableSet(Player,String,Any?,Any?)" to VariableSet.playerDispatcher,
        "variableSet(NPC,String,Any?,Any?)" to VariableSet.npcDispatcher,
    )

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
                val name = script.substringAfterLast("|")
                val instance = loadScript(name)
                scriptCount++
                if (!script.contains("|")) {
                    continue
                }
                val methods = script.split("|").dropLast(1)
                for (method in methods) {
                    val dispatcher = dispatchers[method] ?: error("Unknown dispatcher for method: $method")
                    dispatcher.load(instance)
                }
            }
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
