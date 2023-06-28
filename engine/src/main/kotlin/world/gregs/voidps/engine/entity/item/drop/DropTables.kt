package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.FastUtilConfiguration
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

@Suppress("UNCHECKED_CAST")
class DropTables {

    private lateinit var tables: Map<String, DropTable>

    fun get(key: String) = tables[key]

    fun getValue(key: String) = tables.getValue(key)

    private val defaultAmount = 1..1

    fun load(parser: YamlParser = get(), path: String = getProperty("dropsPath")): DropTables {
        timedLoad("drop table") {
            val config = object : FastUtilConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    value as Map<String, Any>
                    super.add(list, if (value.containsKey("drops")) {
                        val type = value["type"] as? TableType ?: TableType.First
                        val roll = value["roll"] as? Int ?: 1
                        val drops = value["drops"] as List<Drop>
                        DropTable(type, roll, drops)
                    } else {
                        ItemDrop(value["id"] as String, value["amount"] as? IntRange ?: defaultAmount, value["chance"] as? Int ?: 1)
                    }, parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        value as Map<String, Any>
                        super.set(map, key, DropTable(
                            value["type"] as? TableType ?: TableType.First,
                            value["roll"] as? Int ?: 1,
                            value["drops"] as List<Drop>
                        ), indent, parentMap)
                    } else {
                        super.set(map, key, when (key) {
                            "type" -> TableType.byName(value as String)
                            "amount" -> if (value is String && value.contains("-")) {
                                value.toIntRange(inclusive = true)
                            } else {
                                value as Int..value
                            }
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            tables = parser.load(path, config)
            tables.size
        }
        return this
    }
}