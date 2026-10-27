package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer

object GraphicLoader : Loader<DefinitionsState<GraphicDefinition>> {
    override val name = "graphics"
    override val label = "graphic config"
    override val files = listOf("definitions.graphics")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(GraphicDefinition::class, GraphicDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<GraphicDefinition> {
        val definitions = GraphicDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this)))
    }

    override fun total(state: DefinitionsState<GraphicDefinition>) = state.ids.size

    override fun install(state: DefinitionsState<GraphicDefinition>) = GraphicDefinitions.install(state)

    fun parse(definitions: Array<GraphicDefinition>, paths: List<String>): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    val params = Int2ObjectOpenHashMap<Any>(0)
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "angle" -> throw IllegalArgumentException("Unknown key 'angle' use 'curve' instead. ${exception()}")
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    require(!ids.containsKey(stringId)) { "Duplicate graphics id found '$stringId' at $path." }
                    require(id != -1) { "Missing id for graphic '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id].stringId = stringId
                    definitions[id].params = params.ifEmpty { null }
                }
            }
        }
        return ids
    }
}
