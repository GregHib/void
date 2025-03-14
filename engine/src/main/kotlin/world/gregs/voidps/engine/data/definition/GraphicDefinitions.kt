package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
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
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    if (section == "gfx") {
                        val id = (value as Long).toInt()
                        ids[key] = id
                        definitions[id].stringId = key
                    } else {
                        when (key) {
                            "id" -> {
                                val id = (value as Long).toInt()
                                ids[section] = id
                                definitions[id].stringId = section
                            }
                            else -> {
                                require(ids.containsKey(section)) { "Unable to find definition '$section', make sure definition id is set first in section." }
                                var extras = definitions[ids.getInt(section)].extras
                                if (extras == null) {
                                    extras = Object2ObjectOpenHashMap(2, Hash.VERY_FAST_LOAD_FACTOR)
                                }
                                (extras as MutableMap<String, Any>)[section] = value
                            }
                        }
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.ids = ids
            ids.size
        }
        return this
    }

}