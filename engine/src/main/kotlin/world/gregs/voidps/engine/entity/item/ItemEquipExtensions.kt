package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

val ItemDefinition.slot: EquipSlot
    get() = this["slot", EquipSlot.None]

val Item.slot: EquipSlot
    get() = def.slot

fun Item.isBrokenEquipment(): Boolean = slot != EquipSlot.None && id.endsWith("_broken")

val ItemDefinition.type: EquipType
    get() = this["type", EquipType.None]

val Item.type: EquipType
    get() = def.type
