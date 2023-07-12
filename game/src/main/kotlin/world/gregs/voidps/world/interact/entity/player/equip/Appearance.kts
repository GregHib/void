package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.Body
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot

on<ItemChanged>({ inventory == "worn_equipment" && needsUpdate(index, it.body) }) { player: Player ->
    player.flagAppearance()
}

fun needsUpdate(index: Int, parts: Body): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}