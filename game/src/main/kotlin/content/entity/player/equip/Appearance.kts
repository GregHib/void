package content.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.Body
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

inventoryChanged { player ->
    if (needsUpdate(index, player.body)) {
        player.flagAppearance()
    }
}

fun needsUpdate(index: Int, parts: Body): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}
