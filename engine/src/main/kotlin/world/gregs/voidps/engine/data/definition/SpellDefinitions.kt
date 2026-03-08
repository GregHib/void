package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.timedLoad

class SpellDefinitions {

    lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    fun load(path: String): SpellDefinitions {
        timedLoad("spell definition") {
            val definitions = Object2ObjectOpenHashMap<String, SpellDefinition>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    val params = Int2ObjectOpenHashMap<Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                    var maxHit = 0
                    var experience = 0.0
                    while (nextPair()) {
                        when (val key = key()) {
                            "clone" -> {
                                val clone = string()
                                require(definitions.containsKey(clone)) { "Unable to find spell with id '$clone'" }
                                params.putAll(definitions[clone]?.params ?: continue)
                            }
                            "exp" -> experience = double()
                            "max_hit" -> maxHit = int()
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    if (params.isEmpty()) {
                        definitions[stringId] = SpellDefinition(maxHit = maxHit, experience = experience, stringId = stringId)
                    } else {
                        definitions[stringId] = SpellDefinition(maxHit = maxHit, experience = experience, stringId = stringId, params = params)
                    }
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}
