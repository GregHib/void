package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.timedLoad

class GearDefinitions {

    private lateinit var definitions: Map<String, List<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    fun load(path: String = Settings["definitions.gearSets"]): GearDefinitions {
        timedLoad("gear definition") {
            val definitions = Object2ObjectOpenHashMap<String, List<GearDefinition>>(100, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    val list = value as List<Map<String, Any>>
                    definitions[key] = ObjectArrayList(list.map { GearDefinition(key, it) })
                }
            }
            Config.decodeFromFile(path, reader)
            this.definitions = definitions
            definitions.size
        }
        return this
    }
}