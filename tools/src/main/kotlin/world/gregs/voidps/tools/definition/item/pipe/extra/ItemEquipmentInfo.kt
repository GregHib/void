package world.gregs.voidps.tools.definition.item.pipe.extra

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.convert.ItemDecoder718
import world.gregs.voidps.tools.definition.item.Extras

class ItemEquipmentInfo(decoder: Array<ItemDefinition>, val cache: Cache) : Pipeline.Modifier<Extras> {

    init {
        // Load equip slots and types
        ItemDecoder718().load(cache)
    }

    private val types = ItemTypes(decoder)

    override fun modify(content: Extras): Extras {
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
