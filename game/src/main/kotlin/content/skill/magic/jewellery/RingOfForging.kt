package content.skill.magic.jewellery

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class RingOfForging : Script {

    init {
        itemOption("Check", "ring_of_forging", "worn_equipment") {
            val charges = remainingCharges(this)
            message("You can smelt ${charges} more ${piece.plural(charges)} of iron ore before a ring melts.")
        }
    }

    companion object {
        private const val CHARGES_USED = "ring_of_forging_charges"
        private const val MAX_CHARGES = 140

        fun guaranteesIronBar(player: Player, bar: String): Boolean = bar == "iron_bar" &&
            player.equipped(EquipSlot.Ring).id == "ring_of_forging" &&
            remainingCharges(player) > 0

        fun useCharge(player: Player) {
            if (!guaranteesIronBar(player, "iron_bar")) {
                return
            }
            val charges = player[CHARGES_USED, 0] + 1
            if (charges < MAX_CHARGES) {
                player[CHARGES_USED] = charges
                return
            }
            player[CHARGES_USED] = 0
            player.message("Your Ring of Forging has melted.")
            player.equipment.remove(EquipSlot.Ring.index, "ring_of_forging")
        }

        private fun remainingCharges(player: Player): Int = (MAX_CHARGES - player[CHARGES_USED, 0]).coerceAtLeast(0)
    }
}
