package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.timedLoad

class HuntModeDefinitions {

    private lateinit var modes: Map<String, HuntModeDefinition>

    fun get(name: String): HuntModeDefinition {
        return modes.getValue(name)
    }

    fun load(path: String = Settings["definitions.huntModes"]): HuntModeDefinitions {
        timedLoad("hunt mode") {
            val modes = Object2ObjectOpenHashMap<String, HuntModeDefinition>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = ConfigMap(50, 10)
            Config.decodeFromFile(path, reader)
            for ((section, map) in reader.sections) {
                modes[section] = HuntModeDefinition.fromMap(map as Map<String, Any>)
            }
            this.modes = modes
            modes.size
        }
        return this
    }

}