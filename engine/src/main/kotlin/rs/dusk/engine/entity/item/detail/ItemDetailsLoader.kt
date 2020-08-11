package rs.dusk.engine.entity.item.detail

import com.google.common.collect.HashBiMap
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType

class ItemDetailsLoader(private val loader: FileLoader, private val decoder: ItemDecoder) : TimedLoader<ItemDetails>("item detail") {

    private var equipmentCount = 0
    private val equipmentIndices = (0 until decoder.size).map {
        val def = decoder.get(it)
        it to if(def != null && (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0)) {
            equipmentCount++
        } else {
            -1
        }
    }.toMap()

    override fun load(args: Array<out Any?>): ItemDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, ItemDetail> = data.mapValues { convert(it.value) }
        val items = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return ItemDetails(items, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): ItemDetail {
        val id: Int by map
        val equipId = equipmentIndices.getOrDefault(id, -1)
        val slot = map["slot"] as? String
        val type = map["type"] as? String
        return ItemDetail(id = id,
            slot = EquipSlot.valueOf(slot ?: "None"),
            type = EquipType.valueOf(type ?: "None"),
            equip = equipId
        )
    }
}