package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.visual.update.player.EquipSlot

fun Player.has(slot: EquipSlot): Boolean = equipment[slot.index].isNotEmpty()

fun Player.equipped(slot: EquipSlot): Item = equipment[slot.index]