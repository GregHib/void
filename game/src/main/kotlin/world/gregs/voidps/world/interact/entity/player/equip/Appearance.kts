package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.network.visual.update.player.Body
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot

itemChange("worn_equipment") { player: Player ->
    if (needsUpdate(index, player.body)) {
        player.flagAppearance()
    }
}

fun needsUpdate(index: Int, parts: Body): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}