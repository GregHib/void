package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

class ItemDefinitions(
    override val decoder: ItemDecoder
) : DefinitionsDecoder<ItemDefinition, ItemDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    private var equipmentCount = 0
    private val equipmentIndices = (0 until decoder.last).associateWith {
        val def = decoder.getOrNull(it)
        if (def != null && (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0)) {
            equipmentCount++
        } else {
            -1
        }
    }

    fun load(loader: FileLoader = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item definition") {
            load(loader.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        this.extras = data.mapValues { (_, value) ->
            val copy = data[value["copy"]]
            val value = if (copy != null) {
                val mut = copy.toMutableMap()
                mut["id"] = value["id"] as Int
                mut
            } else {
                value
            }
            value.mapValues {
                when (it.key) {
                    "slot" -> EquipSlot.valueOf(it.value as String)
                    "type" -> EquipType.valueOf(it.value as String)
                    "kept" -> ItemKept.valueOf(it.value as String)
                    else -> it.value
                }
            }.toMutableMap().apply {
                this["equip"] = equipmentIndices.getOrDefault(value["id"] as Int, -1)
            }
        }.toMap()
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        return names.size
    }
}