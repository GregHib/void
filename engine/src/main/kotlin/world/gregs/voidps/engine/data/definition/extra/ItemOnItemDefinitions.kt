package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.DefinitionIdsConfig
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class ItemOnItemDefinitions {

    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    fun get(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)] ?: emptyList()

    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("itemOnItemDefinitionsPath")): ItemOnItemDefinitions {
        timedLoad("item on item definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>()
            val config = object : DefinitionIdsConfig() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int) {
                    if (indent == 0) {
                        val definition = ItemOnItemDefinition(value as Map<String, Any>)
                        val usable = definition.requires.toMutableList()
                        usable.addAll(definition.one)
                        usable.addAll(definition.remove)
                        for (a in usable.indices) {
                            for (b in usable.indices) {
                                if (a != b) {
                                    val one = usable[a]
                                    val two = usable[b]
                                    val list = definitions.getOrPut(id(one, two)) { mutableListOf() }
                                    if (!list.contains(definition)) {
                                        list.add(definition)
                                    }
                                }
                            }
                        }
                    } else {
                        super.set(map, key, value, indent)
                    }
                }
            }
            val size = parser.load<Map<String, Any>>(path, config).size
            this.definitions = definitions
            size
        }
        return this
    }

    companion object {
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }

}