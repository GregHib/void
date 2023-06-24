package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.config.SpellDefinition
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.FastUtilConfiguration
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class SpellDefinitions {

    private lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("spellDefinitionsPath")): SpellDefinitions {
        timedLoad("spell definition") {
            val definitions = Object2ObjectOpenHashMap<String, SpellDefinition>()
            val config = object : FastUtilConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int) {
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            SpellDefinition(key, value as MutableMap<String, Any>)
                        } else {
                            SpellDefinition(stringId = key)
                        }
                    } else {
                        super.set(map, key, value, indent)
                    }
                }
            }
            parser.load<Any>(path, config)
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}