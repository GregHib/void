package content.skill.firemaking

import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.collections.any

object Light {
    fun hasLightSource(player: Player): Boolean {
        if (player.inventory.items.any { it.isNotEmpty() && (isFiremakingCape(it.id) || Rows.getOrNull("extinguish.${it.id}") != null) }) {
            return true
        }
        if (player.equipped(EquipSlot.Shield).id == "lit_bug_lantern") {
            return true
        }
        return isFiremakingCape(player.equipped(EquipSlot.Cape).id)
    }

    private fun isFiremakingCape(item: String) = item == "firemaking_cape" || item == "firemaking_cape_t"

    fun extinguish(player: Player) {
        player.inventory.transaction {
            for (index in inventory.indices) {
                val item = inventory[index]
                val row = Rows.getOrNull("extinguish.${item.id}") ?: continue
                val type = row.string("type")
                if (type != "candle" && type != "torch") {
                    // Don't extinguish lanterns
                    continue
                }
                val unlit = row.item(".unlit")
                replace(index, item.id, unlit)
            }
        }
    }
}
