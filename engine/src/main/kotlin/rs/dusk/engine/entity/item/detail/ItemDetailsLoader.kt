package rs.dusk.engine.entity.item.detail

import com.google.common.collect.HashBiMap
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.ItemDrop

class ItemDetailsLoader(private val loader: FileLoader, private val decoder: ItemDecoder) : TimedLoader<ItemDetails>("item detail") {

    private var equipmentCount = 0
    private val equipmentIndices = (0 until decoder.size).map {
        val def = decoder.getOrNull(it)
        it to if (def != null && (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0)) {
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
        val weight = map["weight"] as? Double ?: 0.0
        val edible = map["edible"] as? Boolean ?: false
        val tradeable = map["tradeable"] as? Boolean ?: true
        val alchable = map["alchable"] as? Boolean ?: true
        val bankable = map["bankable"] as? Boolean ?: true
        val individual = map["individual"] as? Boolean ?: false
        val limit = map["limit"] as? Int ?: -1
        val demise = map["demise"] as? String ?: "Drop"
        val destroy = map["destroy"] as? String ?: ""
        val examine = map["examine"] as? String ?: ""
        return ItemDetail(id = id,
            slot = EquipSlot.valueOf(slot ?: "None"),
            type = EquipType.valueOf(type ?: "None"),
            equip = equipId,
            weight = weight,
            edible = edible,
            tradeable = tradeable,
            alchable = alchable,
            bankable = bankable,
            individual = individual,
            limit = limit,
            demise = ItemDrop.valueOf(demise),
            destroy = destroy,
            examine = examine
        )
    }
}