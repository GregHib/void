package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.config.DiangoCodeDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class DiangoCodeDefinitions {

    private lateinit var definitions: Map<String, DiangoCodeDefinition>

    fun get(code: String) = definitions[code] ?: DiangoCodeDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("diangoCodeDefinitionsPath"), itemDefinitions: ItemDefinitions? = null): DiangoCodeDefinitions {
        timedLoad("diango code definition") {
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, if (value is Map<*, *>) {
                        val id = value["item"] as String
                        if (itemDefinitions != null && !itemDefinitions.contains(id)) {
                            logger.warn { "Invalid diango item id: $id" }
                        }
                        Item(id, value["amount"] as? Int ?: 1)
                    } else {
                        Item(value as String, amount = 1)
                    }, parentMap)
                }
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        super.set(map, key, DiangoCodeDefinition(value as Map<String, Any>), indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            val definitions = yaml.load<Any>(path, config) as Map<String, DiangoCodeDefinition>
            this.definitions = definitions
            definitions.size
        }
        return this
    }

    companion object {
        private val logger = InlineLogger()
    }

}