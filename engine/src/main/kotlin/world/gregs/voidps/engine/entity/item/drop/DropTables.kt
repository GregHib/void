package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

@Suppress("UNCHECKED_CAST")
class DropTables {

    private lateinit var tables: Map<String, DropTable>

    fun get(key: String) = tables[key]

    fun getValue(key: String) = tables.getValue(key)

    fun load(yaml: Yaml = get(), path: String = Settings["spawns.drops"], itemDefinitions: ItemDefinitions? = null): DropTables {
        timedLoad("drop table") {
            val config = object : YamlReaderConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    value as Map<String, Any>
                    super.add(list, if (value.containsKey("drops")) DropTable(value) else ItemDrop(value, itemDefinitions), parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        value as Map<String, Any>
                        super.set(map, key, DropTable(value), indent, parentMap)
                    } else {
                        super.set(map, key, when (key) {
                            "type" -> TableType.byName(value as String)
                            "amount", "charges" -> if (value is String && value.contains("-")) {
                                value.toIntRange(inclusive = true)
                            } else {
                                value as Int..value
                            }
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            tables = yaml.load(path, config)
            tables.size
        }
        return this
    }
}