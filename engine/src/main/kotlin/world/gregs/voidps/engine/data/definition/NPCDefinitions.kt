package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString

class NPCDefinitions(
    override var definitions: Array<NPCDefinition>
) : DefinitionsDecoder<NPCDefinition> {

    override var ids: Map<String, Int> = emptyMap()
    var groups: Map<String, Set<String>> = emptyMap()

    override fun empty() = NPCDefinition.EMPTY

    fun load(paths: List<String>): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
            val groups = Object2ObjectOpenHashMap<String, MutableSet<String>>(32)
            ids.defaultReturnValue(-1)
            for (path in paths) {
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
                                "categories" -> {
                                    val categories = ObjectLinkedOpenHashSet<String>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                    while (nextElement()) {
                                        val category = string()
                                        categories.add(category)
                                        groups.getOrPut(category) { ObjectOpenHashSet(2, Hash.VERY_FAST_LOAD_FACTOR) }.add(stringId)
                                    }
                                    extras["categories"] = categories
                                }
                                else -> extras[key] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate npc id found '$stringId' at $path." }
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
            }
            this.groups = groups
            this.ids = ids
            ids.size
        }
        return this
    }

}