package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>
) : DefinitionsDecoder<InventoryDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = InventoryDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.inventories"], itemDefs: ItemDefinitions = get()): InventoryDefinitions {
        timedLoad("inventory extra") {
            val ids = Object2IntOpenHashMap<String>()
            val config = object : DefinitionConfig<InventoryDefinition>(ids, definitions) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "defaults" && value is List<*>) {
                        val id = map["id"] as Int
                        value as List<Map<String, Int>>
                        if (id !in definitions.indices) {
                            return
                        }
                        val def = definitions[id]
                        def.length = map["length"] as? Int ?: def.length
                        def.ids = IntArray(def.length) { itemDefs.get(value.getOrNull(it)?.keys?.first() ?: "").id }
                        def.amounts = IntArray(def.length) { value.getOrNull(it)?.values?.first() ?: 0 }
                    }
                    super.set(map, key, value, indent, parentMap)
                }
            }
            yaml.load<Any>(path, config)
            this.ids = ids
            ids.size
        }
        return this
    }
}

fun InventoryDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}