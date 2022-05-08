package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.entity.definition.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ItemOnItemDefinitions {

    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    fun get(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)] ?: emptyList()

    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    fun load(storage: FileStorage = get(), path: String = getProperty("itemOnItemDefinitionsPath")): ItemOnItemDefinitions {
        timedLoad("item on item definition") {
            val data: Map<String, Any> = storage.load(path)
            load(data.mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        val map = mutableMapOf<String, MutableList<ItemOnItemDefinition>>()
        for ((_, value) in data) {
            val definition = ItemOnItemDefinition(value)
            val usable = definition.requires.toMutableList()
            usable.addAll(definition.one)
            usable.addAll(definition.remove)
            for (a in usable.indices) {
                for (b in usable.indices) {
                    if (a != b) {
                        val one = usable[a]
                        val two = usable[b]
                        val list = map.getOrPut(id(one, two)) { mutableListOf() }
                        if (!list.contains(definition)) {
                            list.add(definition)
                        }
                    }
                }
            }
        }
        definitions = map
        return definitions.size
    }

    companion object {
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }

}