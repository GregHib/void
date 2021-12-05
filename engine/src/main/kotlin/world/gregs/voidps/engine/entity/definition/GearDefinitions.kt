package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.config.GearDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class GearDefinitions {

    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    fun get(style: String, level: Int): List<GearDefinition> = definitions[style]?.filter { it.levels.contains(level) } ?: emptyList()

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
            val style = item["style"] as String
            val list = map.getOrPut(style) { mutableListOf() }
            list.add(GearDefinition(style, item))
        }
        definitions = map
        return definitions.size
    }

}