package content.skill.magic.weapon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.discharge
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class NatureStaff : Script {

    init {
        playerSpawn {
            sendVariable("nature_staff_charges")
        }

        itemOption("Inspect", "nature_staff") {
            val charges = inventory.charges(this, it.slot)
            message("The staff has ${if (charges == 0) "no" else charges} ${"charge".plural(charges)}.")
        }

        itemOption("Empty", "nature_staff") {
            val charges = inventory.charges(this, it.slot)
            if (charges == 0) {
                message("The staff has no charges for your to remove.")
                return@itemOption
            }
            val success = inventory.transaction {
                val added = addToLimit("nature_rune", charges)
                if (added <= 0) {
                    error = TransactionError.Deficient(charges)
                } else {
                    discharge(this@itemOption, it.slot, amount = added)
                }
            }
            if (success) {
                message("You remove charges from the staff and retrieve some nature runes.")
            } else {
                inventoryFull()
            }
        }

        itemOnItem("nature_rune", "nature_staff") { fromItem, toItem, fromSlot, toSlot ->
            val maximum = toItem.def.getOrNull<Int>("charges_max") ?: return@itemOnItem
            val spaces = maximum - inventory.charges(this, toSlot)
            val count = inventory.count(fromItem.id).coerceAtMost(spaces)
            if (count <= 0) {
                message("The staff already has the maximum amount of charges.")
                return@itemOnItem
            }
            val success = inventory.transaction {
                remove(fromItem.id, count)
                charge(this@itemOnItem, toSlot, count)
            }
            if (success) {
                message("You charge the staff with nature runes.")
            }
        }
    }
}
