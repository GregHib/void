package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>
) : DefinitionsDecoder<InventoryDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = InventoryDefinition.EMPTY

    fun load(path: String = Settings["definitions.inventories"], itemDefs: ItemDefinitions = get()): InventoryDefinitions {
        timedLoad("inventory extra") {
            val ids = Object2IntOpenHashMap<String>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val section = section()
                    var id = -1
                    val extras = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                    val items = IntArrayList()
                    val amounts = IntArrayList()
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "defaults" -> {
                                while (nextPair()) {
                                    val item = key()
                                    amounts.add(int())
                                    items.add(itemDefs.get(item).id)
                                }
                            }
                            else -> extras[key] = value()
                        }
                    }
                    if (id > -1) {
                        ids[section] = id
                        definitions[id].length = items.size
                        definitions[id].ids = IntArray(items.size) { items.getInt(it) }
                        definitions[id].amounts = IntArray(amounts.size) { amounts.getInt(it) }
                        definitions[id].extras = extras
                    }
                }
            }
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