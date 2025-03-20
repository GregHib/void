package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.timedLoad

class NPCDefinitions(
    override var definitions: Array<NPCDefinition>
) : DefinitionsDecoder<NPCDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = NPCDefinition.EMPTY

    fun load(path: String = Settings["definitions.npcs"]): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
            ids.defaultReturnValue(-1)
            Config.fileReader(path, 150) {
                while (nextSection()) {
                    val stringId = section()
                    val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                    var id = -1
                    while (nextPair()) {
                        when (val key = key()) {
                            "clone" -> {
                                val name = string()
                                val npc = ids.getInt(name)
                                require(npc >= 0) { "Cannot find npc id to clone '$name'" }
                                val definition = definitions[npc]
                                extras.putAll(definition.extras ?: continue)
                            }
                            "id" -> id = int()
                            "pickpocket" -> extras[key] = Pocket(this)
                            "fishing" -> {
                                val spots = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                while (nextEntry()) {
                                    val type = key()
                                    val spot = Spot(this)
                                    spots[type] = spot
                                }
                                extras[key] = spots
                            }
                            else -> extras[key] = value()
                        }
                    }
                    ids[stringId] = id
                    definitions[id].stringId = stringId
                    if (extras.isNotEmpty()) {
                        if (definitions[id].extras != null) {
                            (definitions[id].extras as MutableMap<String, Any>).putAll(extras)
                        } else {
                            definitions[id].extras = extras
                        }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

}