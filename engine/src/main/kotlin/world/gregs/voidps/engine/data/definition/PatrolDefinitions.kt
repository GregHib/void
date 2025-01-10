package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.PatrolDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class PatrolDefinitions {

    private lateinit var definitions: Map<String, PatrolDefinition>

    fun get(key: String) = definitions[key] ?: PatrolDefinition()

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.patrols"]): PatrolDefinitions {
        timedLoad("patrol definition") {
            val definitions = Object2ObjectOpenHashMap<String, PatrolDefinition>()
            val config = object : YamlReaderConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            PatrolDefinition(key, value as MutableMap<String, Any>)
                        } else {
                            PatrolDefinition(stringId = key)
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