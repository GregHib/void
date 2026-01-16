package content.quest.member.enakhras_lament

import content.entity.player.dialogue.type.statement
import content.skill.magic.jewellery.jewelleryTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Camulet(val areas: AreaDefinitions) : Script {

    init {
        itemOption("Rub", "camulet") {
            if (jewelleryTeleport(this, it.inventory, it.slot, areas["camulet_teleport"])) {
                message("You rub the amulet...")
            } else {
                statement("Your Camulet has run out of teleport charges. You can renew them by applying camel dung.")
            }
        }

        itemOption("Check-charge", "camulet") {
            if (it.inventory != "inventory") {
                return@itemOption
            }
            val charges = inventory.charges(this, it.slot)
            message("Your Camulet has $charges ${"charge".plural(charges)} left.")
            if (charges == 0) {
                message("You can recharge it by applying camel dung.")
            }
        }

        itemOnItem("ugthanki_dung", "camulet") { fromItem, _, fromSlot, toSlot ->
            val slot = if (fromItem.id == "camulet") fromSlot else toSlot
            val charges = inventory.charges(this, slot)
            if (charges == 4) {
                message("Your Camulet already has 4 charges.")
                return@itemOnItem
            }
            if (inventory.replace("ugthanki_dung", "bucket")) {
                message("You recharge the Camulet using camel dung. Yuck!")
                set("camulet_charges", 4)
            }
        }
    }
}
