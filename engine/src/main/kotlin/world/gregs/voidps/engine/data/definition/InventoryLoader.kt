package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer

object InventoryLoader : Loader<DefinitionsState<InventoryDefinition>> {
    override val name = "inventories"
    override val files = listOf("definitions.inventories", "definitions.shops")
    override val usesCache = true
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(InventoryDefinition::class, InventoryDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<InventoryDefinition> {
        val definitions = InventoryDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.list("definitions.inventories"), context.list("definitions.shops")))
    }

    override fun install(state: DefinitionsState<InventoryDefinition>) = InventoryDefinitions.install(state)

    fun parse(definitions: Array<InventoryDefinition>, paths: List<String>, shopPaths: List<String>): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>()
        for (path in paths) {
            loadInventories(definitions, path, ids, shop = false)
        }
        for (path in shopPaths) {
            loadInventories(definitions, path, ids, shop = true)
        }
        return ids
    }

    private fun loadInventories(definitions: Array<InventoryDefinition>, path: String, ids: Object2IntOpenHashMap<String>, shop: Boolean) {
        Config.fileReader(path) {
            while (nextSection()) {
                val stringId = section()
                var invId = -1
                val params = Int2ObjectOpenHashMap<Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                if (shop) {
                    params[Params.SHOP] = true
                }
                var itemIds: IntArray? = null
                var amounts: IntArray? = null
                var length = -1
                while (nextPair()) {
                    when (val key = key()) {
                        "id" -> invId = int()
                        "defaults" -> {
                            var index = 0
                            require(invId != -1) { "Inventory '$stringId' must declare 'id' before 'defaults' at $path." }
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
                                require(ItemDefinitions.contains(id)) { "Unable to find inventory item with id '$id' at $path." }
                                itemIds[index] = ItemDefinitions.get(id).id
                                amounts[index++] = amount
                            }
                        }
                        "length" -> length = int()
                        "stack", "currency", "title" -> params[Params.id(key)] = string()
                        "width", "height" -> params[Params.id(key)] = int()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                if (invId > -1) {
                    require(!ids.containsKey(stringId)) { "Duplicate inventory found '$stringId' at $path." }
                    ids[stringId] = invId
                    if (params.isNotEmpty()) {
                        definitions[invId].params = params
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
