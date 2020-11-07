package rs.dusk.tools.definition.item.pipe.extra

import rs.dusk.cache.Cache
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.tools.Pipeline
import rs.dusk.tools.convert.ItemDecoder718
import rs.dusk.tools.definition.item.ItemExtras

class ItemEquipmentInfo(decoder: ItemDecoder, val cache: Cache) : Pipeline.Modifier<ItemExtras> {

    private val decoder718 = ItemDecoder718(cache)
    init {
        // Load equip slots and types
        repeat(decoder718.size) { id ->
            decoder718.get(id)
        }
    }

    private val types = ItemTypes(decoder)

    override fun modify(content: ItemExtras): ItemExtras {
        val (builder, extras) = content
        val (id, _, _, _, _) = builder
        val slot = ItemDecoder718.equipSlots[id]
        if (slot != null) {
            var s = EquipSlot.by(slot)
            if (id == 11277) {
                s = EquipSlot.Hat
            }
            if (s == EquipSlot.None) {
                println("Unknown slot $slot $id")
            } else {
                extras["slot"] = s.name
            }
        }
        val type = types.getEquipType(id)
        if (type != EquipType.None) {
            extras["type"] = type.name
        }
        return content
    }
}