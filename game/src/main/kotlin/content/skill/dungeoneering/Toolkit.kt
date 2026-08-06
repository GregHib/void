package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Toolkit : Script {
    init {
        itemOption("Open", "toolkit_dungeoneering") {
            inventory.transaction {
                remove(it.slot, "toolkit_dungeoneering")
                add("novite_pickaxe")
                add("novite_hatchet")
                add("tinderbox_dungeoneering")
                add("hammer_dungeoneering")
                add("knife_dungeoneering")
                add("vial_dungeoneering")
                add("fly_fishing_rod_dungeoneering")
                add("feather_dungeoneering", 50)
            }
            when (inventory.transaction.error) {
                is TransactionError.Full -> inventoryFull() // TODO proper message
                TransactionError.None -> {
                    // https://youtu.be/C5G4sXKrVSI?t=140
                    message("You take out a variety of useful items from the toolkit.")
                }
                else -> return@itemOption
            }
        }
    }
}