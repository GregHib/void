package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class HuntModeDefinitions {

    private lateinit var modes: Map<String, HuntModeDefinition>

    fun get(name: String): HuntModeDefinition {
        return modes.getValue(name)
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.huntModes"]): HuntModeDefinitions {
        timedLoad("hunt mode") {
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        super.set(map, key, HuntModeDefinition.fromMap(value as Map<String, Any>), indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            this.modes = yaml.load(path, config)
            modes.size
        }
        return this
    }

}