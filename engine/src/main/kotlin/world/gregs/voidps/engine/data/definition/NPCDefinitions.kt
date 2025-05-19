package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.timedLoad

class NPCDefinitions(
    override var definitions: Array<NPCDefinition>
) : DefinitionsDecoder<NPCDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = NPCDefinition.EMPTY

    fun load(
        paths: List<String>,
        dropTables: DropTables? = null,
        animationDefinitions: AnimationDefinitions? = null,
        soundDefinitions: SoundDefinitions? = null
    ): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
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
                                        categories.add(string())
                                    }
                                    extras["categories"] = categories
                                }
                                "drop_table" -> {
                                    val table = string()
                                    require(dropTables == null || table.isBlank() || dropTables.get("${table}_drop_table") != null) { "Drop table '$table' not found for npc $stringId" }
                                    extras[key] = table
                                }
                                "combat_anims" -> {
                                    val name = string()
                                    if (animationDefinitions != null && name.isNotBlank()) {
                                        // Attack isn't always required because of weapon style
                                        require(animationDefinitions.contains("${name}_defend")) { "No combat animation ${name}_defend found for npc $stringId" }
                                        require(animationDefinitions.contains("${name}_death")) { "No combat animation ${name}_death found for npc $stringId" }
                                    }
                                    extras[key] = name
                                }
                                "combat_sounds" -> {
                                    val name = string()
                                    if (soundDefinitions != null && name.isNotBlank()) {
                                        require(soundDefinitions.contains("${name}_attack") || soundDefinitions.contains("${name}_defend") || soundDefinitions.contains("${name}_death")) { "No combat sounds '${name}' found for npc $stringId" }
                                    }
                                    extras[key] = name
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
            this.ids = ids
            ids.size
        }
        return this
    }

}