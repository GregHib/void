package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>
) : DefinitionsDecoder<InventoryDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = InventoryDefinition.EMPTY

    fun load(paths: List<String>, shopPaths: List<String>, itemDefs: ItemDefinitions = get()): InventoryDefinitions {
        timedLoad("inventory extra") {
            val ids = Object2IntOpenHashMap<String>()
            for (path in paths) {
                loadInventories(path, itemDefs, ids, shop = false)
            }
            for (path in shopPaths) {
                loadInventories(path, itemDefs, ids, shop = true)
            }
            this.ids = ids
            ids.size
        }
        return this
    }

    private fun loadInventories(path: String, itemDefs: ItemDefinitions, ids: Object2IntOpenHashMap<String>, shop: Boolean) {
        Config.fileReader(path) {
            while (nextSection()) {
                val stringId = section()
                var invId = -1
                val extras = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                if (shop) {
                    extras["shop"] = true
                }
                while (nextPair()) {
                    when (val key = key()) {
                        "id" -> invId = int()
                        "defaults" -> {
                            val defaults = ObjectArrayList<Item>()
                            while (nextElement()) {
                                var id = ""
                                var amount = 0
                                while (nextEntry()) {
                                    when (val itemKey = key()) {
                                        "id" -> id = string()
                                        "amount" -> amount = int()
                                        else -> throw IllegalArgumentException("Unexpected key: '$itemKey' ${exception()}")
                                    }
                                }
                                require(itemDefs.contains(id)) { "Unable to find inventory item with id '$id' at $path." }
                                val item = Item(id, amount)
                                defaults.add(item)
                            }
                            extras[key] = defaults
                        }
                        "shop" -> {
                            boolean()
                            println("TODO remove shop at ${exception()}")
                        }
                        "stack", "currency" -> extras[key] = string()
                        "width", "height" -> extras[key] = int()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                if (invId > -1) {
                    require(!ids.containsKey(stringId)) { "Duplicate inventory found '$stringId' at $path." }
                    ids[stringId] = invId
                    definitions[invId].extras = extras
                    definitions[invId].stringId = stringId
                }
            }
        }
    }
}