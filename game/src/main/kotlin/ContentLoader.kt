import com.github.michaelbull.logging.InlineLogger
import content.skill.thieving.Stole
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.dispatch.Dispatcher
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.TimerApi
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

/**
 * Loads content scripts from a precomputed scripts.txt list made by scriptMetadata gradle build task
 * Script instances are passed to their relevant [Dispatcher] when they contain an overridden method (also listed in the precomputed list)
 */
object ContentLoader {
    private val logger = InlineLogger()

    private val dispatchers = mutableMapOf<String, Dispatcher<*>>(
        method("stole", "Player", "GameObject", "Item") to Stole.dispatcher,
        method("Dialogue.talk", "Player", "NPC") to Operation.talkDispatcher,
        method("operate", "Player", "Player", "String") to Operation.playerPlayerDispatcher,
        method("operate", "Player", "NPC", "String") to Operation.playerNpcDispatcher,
        method("operate", "Player", "GameObject", "String") to Operation.playerObjectDispatcher,
        method("operate", "Player", "FloorItem", "String") to Operation.playerFloorItemDispatcher,
        method("operate", "NPC", "Player", "String") to Operation.npcPlayerDispatcher,
        method("operate", "NPC", "NPC", "String") to Operation.npcNpcDispatcher,
        method("operate", "NPC", "GameObject", "String") to Operation.npcObjectDispatcher,
        method("operate", "NPC", "FloorItem", "String") to Operation.npcFloorItemDispatcher,
        method("approach", "Player", "Player", "String") to Approachable.playerPlayerDispatcher,
        method("approach", "Player", "NPC", "String") to Approachable.playerNpcDispatcher,
        method("approach", "Player", "GameObject", "String") to Approachable.playerObjectDispatcher,
        method("approach", "Player", "FloorItem", "String") to Approachable.playerFloorItemDispatcher,
        method("approach", "NPC", "Player", "String") to Approachable.npcPlayerDispatcher,
        method("approach", "NPC", "NPC", "String") to Approachable.npcNpcDispatcher,
        method("approach", "NPC", "GameObject", "String") to Approachable.npcObjectDispatcher,
        method("approach", "NPC", "FloorItem", "String") to Approachable.npcFloorItemDispatcher,
        method("start", "Player", "String", "Boolean", returnType = "Int") to TimerApi.playerStartDispatcher,
        method("tick", "Player", "String", returnType = "Int") to TimerApi.playerTickDispatcher,
        method("stop", "Player", "String", "Boolean") to TimerApi.playerStopDispatcher,
        method("start", "NPC", "String", "Boolean", returnType = "Int") to TimerApi.npcStartDispatcher,
        method("tick", "NPC", "String", returnType = "Int") to TimerApi.npcTickDispatcher,
        method("stop", "NPC", "String", "Boolean") to TimerApi.npcStopDispatcher,
        method("start", "Character", "String", "Boolean", returnType = "Int") to TimerApi.characterStartDispatcher,
        method("tick", "Character", "String", returnType = "Int") to TimerApi.characterTickDispatcher,
        method("stop", "Character", "String", "Boolean") to TimerApi.characterStopDispatcher,
        method("start", "String", returnType = "Int") to TimerApi.worldStartDispatcher,
        method("tick", "String", returnType = "Int") to TimerApi.worldTickDispatcher,
        method("stop", "String", "Boolean") to TimerApi.worldStopDispatcher,
    )

    fun load() {
        var start = System.currentTimeMillis()
        val matches = ContentLoader::class.java.getResourceAsStream("matches.txt")?.bufferedReader() ?: error("No auto-generated matches file found, make sure 'gradle scriptMetadata' is correctly running")
        Wildcards.load(matches)
        logger.info { "Loaded ${Wildcards.size} ${"wildcard".plural(Wildcards.size)} in ${System.currentTimeMillis() - start}ms" }
        start = System.currentTimeMillis()
        val scripts = ContentLoader::class.java.getResourceAsStream("scripts.txt")?.bufferedReader() ?: error("No auto-generated script file found, make sure 'gradle scriptMetadata' is correctly running")
        val scriptCount: Int
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
            logger.error { "Otherwise make sure the return type is written explicitly." }
            exitProcess(1)
        }

        if (scriptCount == 0) {
            throw NoSuchFileException("No content scripts found.")
        }
        logger.info { "Loaded $scriptCount ${"script".plural(scriptCount)} in ${System.currentTimeMillis() - start}ms" }
    }

    private fun method(name: String, vararg argTypes: String, returnType: String = "", annotation: String? = null) = buildString {
        if (annotation != null) {
            append(annotation).append("@")
        }
        append(name)
        append("(").append(argTypes.joinToString(",")).append(")")
        if (returnType.isNotBlank()) {
            append(":").append(returnType)
        }
    }

    fun clear() {
        Api.clear()
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
