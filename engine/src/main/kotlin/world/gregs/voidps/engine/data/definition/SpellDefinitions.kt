package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class SpellDefinitions {

    private lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.spells"]): SpellDefinitions {
        timedLoad("spell definition") {
            val definitions = Object2ObjectOpenHashMap<String, SpellDefinition>()
            val config = object : YamlReaderConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            SpellDefinition(key, value as MutableMap<String, Any>)
                        } else {
                            SpellDefinition(stringId = key)
                        }
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}