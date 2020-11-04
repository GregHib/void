package rs.dusk.engine.entity.item.detail

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.ItemDrop

class ItemDefinitionLoader(private val loader: FileLoader, private val decoder: ItemDecoder) : TimedLoader<ItemDefinitions>("item definition") {

    private var equipmentCount = 0
    private val equipmentIndices = (0 until decoder.size).map {
        val def = decoder.getOrNull(it)
        it to if (def != null && (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0)) {
            equipmentCount++
        } else {
            -1
        }
    }.toMap()

    override fun load(args: Array<out Any?>): ItemDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val map = data.mapValues { entry ->
            entry.value.mapValues { convert(it.key, it.value) }.toMutableMap().apply {
                this["equip"] = equipmentIndices.getOrDefault(entry.value["id"] as Int, -1)
            }
        }.toMap()
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return ItemDefinitions(decoder, map, names)
    }

    fun convert(key: String, value: Any): Any {
        return when (key) {
            "slot" -> EquipSlot.valueOf(value as String)
            "type" -> EquipType.valueOf(value as String)
            "demise" -> ItemDrop.valueOf(value as String)
            else -> value
        }
    }
}