package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.yaml.DefinitionIdsConfig
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class ItemOnItemDefinitions {

    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    fun get(one: Item, two: Item) = getOrNull(one, two) ?: emptyList()

    fun getOrNull(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)]

    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["itemOnItemDefinitionsPath"], itemDefinitions: ItemDefinitions = get()): ItemOnItemDefinitions {
        timedLoad("item on item definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>()
            var count = 0
            val config = object : DefinitionIdsConfig() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, if (value is Map<*, *>) {
                        val id = value["item"] as String
                        if (!itemDefinitions.contains(id)) {
                            logger.warn { "Invalid item-on-item id: $id" }
                        }
                        Item(id, value["amount"] as? Int ?: 1)
                    } else {
                        Item(value as String, amount = 1)
                    }, parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
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
                                    val list = definitions.getOrPut(id(one, two)) { ObjectArrayList(2) }
                                    if (!list.contains(definition)) {
                                        list.add(definition)
                                    }
                                }
                            }
                        }
                        count++
                    } else {
                        super.set(map, key, when (key) {
                            "skill" -> Skill.valueOf((value as String).toSentenceCase())
                            "chance" -> (value as String).toIntRange()
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = definitions
            count
        }
        return this
    }

    companion object {
        private val logger = InlineLogger()
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }

}