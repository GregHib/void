package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.timedLoad

class SpellDefinitions {

    private lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    fun load(path: String = Settings["definitions.spells"]): SpellDefinitions {
        timedLoad("spell definition") {
            val definitions = Object2ObjectOpenHashMap<String, SpellDefinition>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    val extras = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                    var maxHit = 0
                    var experience = 0.0
                    while (nextPair()) {
                        when (val key = key()) {
                            "clone" -> {
                                val clone = string()
                                require(definitions.containsKey(clone)) { "Unable to find spell with id '$clone'" }
                                extras.putAll(definitions[clone]?.extras ?: continue)
                            }
                            "exp" -> experience = double()
                            "max_hit" -> maxHit = int()
                            else -> extras[key] = value()
                        }
                    }
                    if (extras.isEmpty()) {
                        definitions[stringId] = SpellDefinition(maxHit = maxHit, experience = experience, stringId = stringId)
                    } else {
                        definitions[stringId] = SpellDefinition(maxHit = maxHit, experience = experience, stringId = stringId, extras = extras)
                    }
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}