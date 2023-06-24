package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.config.GearDefinition
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class GearDefinitions {

    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    fun load(parser: YamlParser = get(), path: String = getProperty("gearDefinitionsPath")): GearDefinitions {
        timedLoad("gear definition") {

            val data: List<Map<String, Any>> = parser.load(path)
            load(data)
        }
        return this
    }

    fun load(data: List<Map<String, Any>>): Int {
        val map = Object2ObjectOpenHashMap<String, MutableList<GearDefinition>>()
        for (item in data) {
            val type = item["type"] as String
            val list = map.getOrPut(type) { mutableListOf() }
            list.add(GearDefinition(type, item))
        }
        definitions = map
        return definitions.size
    }

}