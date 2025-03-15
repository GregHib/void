package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad

class GraphicDefinitions(
    override var definitions: Array<GraphicDefinition>
) : DefinitionsDecoder<GraphicDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = GraphicDefinition.EMPTY

    fun load(path: String = Settings["definitions.graphics"]): GraphicDefinitions {
        timedLoad("graphic extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextSection()) {
                    val section = section()
                    if (section == "gfx") {
                        while (nextPair()) {
                            val key = key()
                            val id = int()
                            ids[key] = id
                            definitions[id].stringId = key
                        }
                    } else {
                        val stringId = section.substring(4)
                        var id = 0
                        val extras = Object2ObjectOpenHashMap<String, Any>(0)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                else -> extras[key] = value()
                            }
                        }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        definitions[id].extras = extras.ifEmpty { null }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

}