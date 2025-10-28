package content.skill.magic.weapon

import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.discharge
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

@Script
class LawStaff : Api {

    init {
        playerSpawn { player ->
            player.sendVariable("law_staff_charges")
        }

        inventoryItem("Inspect", "law_staff", "inventory") {
            val charges = player.inventory.charges(player, slot)
            player.message("The staff has ${if (charges == 0) "no" else charges} ${"charge".plural(charges)}.")
        }

        inventoryItem("Empty", "law_staff", "inventory") {
            val charges = player.inventory.charges(player, slot)
            if (charges == 0) {
                player.message("The staff has no charges for your to remove.")
                return@inventoryItem
            }
            val success = player.inventory.transaction {
                val added = addToLimit("law_rune", charges)
                if (added <= 0) {
                    error = TransactionError.Deficient(charges)
                } else {
                    discharge(player, slot, added)
                }
            }
            println(player.inventory.charges(player, slot))
            if (success) {
                player.message("You remove charges from the staff and retrieve some law runes.")
            } else {
                player.inventoryFull()
            }
        }

        itemOnItem("law_rune", "law_staff") { player ->
            val maximum = toItem.def.getOrNull<Int>("charges_max") ?: return@itemOnItem
            val spaces = maximum - player.inventory.charges(player, toSlot)
            val count = player.inventory.count(fromItem.id).coerceAtMost(spaces)
            if (count <= 0) {
                player.message("The staff already has the maximum amount of charges.")
                return@itemOnItem
            }
            val success = player.inventory.transaction {
                remove(fromItem.id, count)
                charge(player, toSlot, count)
            }
            if (success) {
                player.message("You charge the staff with law runes.")
            }
        }
    }
}
