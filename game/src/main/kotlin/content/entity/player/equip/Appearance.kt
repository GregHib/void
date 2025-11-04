package content.entity.player.equip

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.network.login.protocol.visual.update.player.Body
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Appearance : Script {

    init {
        slotChanged {
            if (needsUpdate(index, body)) {
                flagAppearance()
            }
        }
    }

    fun needsUpdate(index: Int, parts: Body): Boolean {
        val slot = EquipSlot.by(index)
        val part = BodyPart.by(slot) ?: return false
        return parts.updateConnected(part)
    }
}
