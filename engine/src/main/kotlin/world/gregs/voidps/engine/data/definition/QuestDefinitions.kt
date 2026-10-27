package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class QuestDefinitions : DefinitionsDecoder<QuestDefinition> {

    override lateinit var definitions: Array<QuestDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String): QuestDefinitions {
        val definitions = Array(300) { QuestDefinition.EMPTY }
        val ids = Object2IntOpenHashMap<String>(256, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path, 600) {
            while (nextSection()) {
                val stringId = section()
                var id = -1
                val params = Int2ObjectOpenHashMap<Any>(16, Hash.VERY_FAST_LOAD_FACTOR)
                var name = ""
                var difficulty = -1
                var members = false
                var questPoints = -1
                while (nextPair()) {
                    when (val key = key()) {
                        "id" -> id = int()
                        "name" -> name = string()
                        "points" -> questPoints = int()
                        "members" -> members = boolean()
                        "difficulty" -> difficulty = int()
                        else -> params[Params.id(key)] = value()
                    }
                }
                require(!ids.containsKey(stringId)) { "Duplicate quest id found '$stringId' at $path." }
                ids[stringId] = id
                if (params.isNotEmpty()) {
                    definitions[id] = QuestDefinition(
                        id = id,
                        stringId = stringId,
                        name = name,
                        questPoints = questPoints,
                        members = members,
                        difficulty = difficulty,
                        params = params,
                    )
                } else {
                    definitions[id] = QuestDefinition(
                        id = id,
                        stringId = stringId,
                        name = name,
                        questPoints = questPoints,
                        members = members,
                        difficulty = difficulty,
                    )
                }
            }
        }
        this.definitions = definitions
        this.ids = ids
        return this
    }

    override fun empty() = QuestDefinition.EMPTY

    internal fun copyFrom(other: QuestDefinitions) {
        definitions = other.definitions
        ids = other.ids
    }
}

object QuestLoader : Loader<QuestDefinitions> {
    val instance = QuestDefinitions()
    override val name = "quests"
    override val file = listOf("definitions.quests")

    override fun load(context: LoadContext): QuestDefinitions = QuestDefinitions().load(context.paths(this).single())

    override fun install(state: QuestDefinitions) = instance.copyFrom(state)
}
