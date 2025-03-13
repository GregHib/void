package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
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
            var count = 0
            val reader = object : ConfigReader(100) {
                override fun set(section: String, key: String, value: Any) {
                    when (section) {
                        "anims" -> {
                            val id = (value as Long).toInt()
                            require(id < definitions.size) { "Invalid animation id '$value' for anim '$key'. Maximum id: ${definitions.size}" }
                            definitions[id].stringId = key
                            ids[key] = id
                            count++
                        }
                        else -> {
                            val stringId = section.removePrefix("anims.")
                            val definition = definitions[ids.getInt(stringId)]
                            when (key) {
                                "id" -> definition.id = (value as Long).toInt()
                                else -> {
                                    val extras: MutableMap<String, Any> = if (definition.extras == null) {
                                        val map = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                        definition.extras = map
                                        map
                                    } else {
                                        definition.extras!!
                                    } as MutableMap<String, Any>
                                    if (value is Long) {
                                        extras[key] = value.toInt()
                                    } else {
                                        extras[key] = value
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.ids = ids
            count
        }
        return this
    }

}