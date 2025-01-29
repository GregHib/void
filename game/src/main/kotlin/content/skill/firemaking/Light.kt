package content.skill.firemaking

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object Light {
    fun hasLightSource(player: Player): Boolean {
        if (player.inventory.items.any { it.id.endsWith("lantern_lit") || it.id.endsWith("candle_lit") || it.id == "firemaking_cape_t" || it.id == "firemaking_cape" }) {
            return true
        }

        val cape = player.equipped(EquipSlot.Cape).id
        return cape == "firemaking_cape" || cape == "firemaking_cape_t"
    }
}