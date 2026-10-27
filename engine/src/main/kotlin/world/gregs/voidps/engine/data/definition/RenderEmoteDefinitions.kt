package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.RenderEmoteDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class RenderEmoteDefinitions : DefinitionsDecoder<RenderEmoteDefinition> {

    override lateinit var definitions: Array<RenderEmoteDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String): RenderEmoteDefinitions {
        val definitions = Array(2000) { RenderEmoteDefinition.EMPTY }
        val ids = Object2IntOpenHashMap<String>(20, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path) {
            while (nextPair()) {
                val stringId = key()
                val id = int()
                require(!ids.containsKey(stringId)) { "Duplicate render emote id found '$stringId' at $path." }
                ids[stringId] = id
                definitions[id] = RenderEmoteDefinition(id = id, stringId = stringId)
            }
        }
        this.definitions = definitions
        this.ids = ids
        return this
    }

    override fun empty() = RenderEmoteDefinition.EMPTY

    internal fun copyFrom(other: RenderEmoteDefinitions) {
        definitions = other.definitions
        ids = other.ids
    }
}

object RenderEmoteLoader : Loader<RenderEmoteDefinitions> {
    val instance = RenderEmoteDefinitions()
    override val name = "render_emotes"
    override val file = listOf("definitions.renderEmotes")

    override fun load(context: LoadContext): RenderEmoteDefinitions = RenderEmoteDefinitions().load(context.paths(this).single())

    override fun install(state: RenderEmoteDefinitions) = instance.copyFrom(state)
}
