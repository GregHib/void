package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>,
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
                var itemIds: IntArray? = null
                var amounts: IntArray? = null
                var length = -1
                while (nextPair()) {
                    when (val key = key()) {
                        "id" -> invId = int()
                        "defaults" -> {
                            var index = 0
                            val definition = definitions[invId]
                            if (length > definition.length) {
                                definition.length = length
                            }
                            itemIds = IntArray(definition.length) { -1 }
                            amounts = IntArray(definition.length)
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
                                itemIds[index] = itemDefs.get(id).id
                                amounts[index++] = amount
                            }
                        }
                        "length" -> length = int()
                        "stack", "currency", "title" -> extras[key] = string()
                        "width", "height" -> extras[key] = int()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                if (invId > -1) {
                    require(!ids.containsKey(stringId)) { "Duplicate inventory found '$stringId' at $path." }
                    ids[stringId] = invId
                    if (extras.isNotEmpty()) {
                        definitions[invId].extras = extras
                    }
                    if (itemIds != null) {
                        definitions[invId].ids = itemIds
                    }
                    if (amounts != null) {
                        definitions[invId].amounts = amounts
                    }
                    definitions[invId].stringId = stringId
                }
            }
        }
    }
}
