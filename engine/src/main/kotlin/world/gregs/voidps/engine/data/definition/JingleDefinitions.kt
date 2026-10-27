package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class JingleDefinitions : DefinitionsDecoder<JingleDefinition> {

    override lateinit var definitions: Array<JingleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>): JingleDefinitions {
        val ids = Object2IntOpenHashMap<String>(200, Hash.VERY_FAST_LOAD_FACTOR)
        val definitions = Array(500) { JingleDefinition.EMPTY }
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            else -> throw IllegalArgumentException("Unknown jingle key: $key")
                        }
                    }
                    require(id != -1) { "Missing id for jingle '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id] = JingleDefinition(id = id, stringId = stringId)
                }
            }
        }
        this.definitions = definitions
        this.ids = ids
        return this
    }

    override fun empty() = JingleDefinition.EMPTY

    internal fun copyFrom(other: JingleDefinitions) {
        definitions = other.definitions
        ids = other.ids
    }
}

object JingleLoader : Loader<JingleDefinitions> {
    val instance = JingleDefinitions()
    override val name = "jingles"
    override val files = listOf("definitions.jingles")

    override fun load(context: LoadContext): JingleDefinitions = JingleDefinitions().load(context.paths(this))

    override fun install(state: JingleDefinitions) = instance.copyFrom(state)
}
