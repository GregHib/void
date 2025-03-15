package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad

class AnimationDefinitions(
    override var definitions: Array<AnimationDefinition>
) : DefinitionsDecoder<AnimationDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = AnimationDefinition.EMPTY

    fun load(path: String = Settings["definitions.animations"]): AnimationDefinitions {
        timedLoad("animation extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextSection()) {
                    val section = section()
                    if (section == "anims") {
                        while (nextPair()) {
                            val key = key()
                            val id = int()
                            require(id < definitions.size) { "Invalid animation id '$id' for anim '$key'. Maximum id: ${definitions.size}" }
                            definitions[id].stringId = key
                            ids[key] = id
                        }
                    } else {
                        val stringId = section.substring(6)
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                else -> extras[key] = value()
                            }
                        }
                        definitions[id].stringId = stringId
                        if (extras.isNotEmpty()) {
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