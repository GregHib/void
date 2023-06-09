package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.config.GearDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class GearDefinitions {

    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    fun load(storage: FileStorage = get(), path: String = getProperty("gearDefinitionsPath")): GearDefinitions {
        timedLoad("gear definition") {
            val data: ArrayList<Map<String, Any>> = storage.load(path)
            load(data)
        }
        return this
    }

    fun load(data: ArrayList<Map<String, Any>>): Int {
        val map = mutableMapOf<String, MutableList<GearDefinition>>()
        for (item in data) {
            val type = item["type"] as String
            val list = map.getOrPut(type) { mutableListOf() }
            list.add(GearDefinition(type, item))
        }
        definitions = Object2ObjectOpenHashMap(map)
        return definitions.size
    }

}