package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.timedLoad

class GraphicDefinitions(
    override var definitions: Array<GraphicDefinition>,
) : DefinitionsDecoder<GraphicDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = GraphicDefinition.EMPTY

    fun load(paths: List<String>): GraphicDefinitions {
        timedLoad("graphic config") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = 0
                        val params = Int2ObjectOpenHashMap<Any>(0)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "angle" -> throw IllegalArgumentException("Unknown key 'angle' use 'curve' instead. ${exception()}")
                                else -> params[Params.id(key)] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate graphics id found '$stringId' at $path." }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        definitions[id].params = params.ifEmpty { null }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}
