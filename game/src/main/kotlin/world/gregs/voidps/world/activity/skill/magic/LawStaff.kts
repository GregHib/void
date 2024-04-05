package world.gregs.voidps.world.activity.skill.magic

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

inventoryItem("Inspect", "law_staff", "inventory") {
    val charges = Degrade.charges(player, player.inventory, slot)
    player.message("The staff has ${if (charges == 0) "no" else charges} ${"charge".plural(charges)}.")
}

inventoryItem("Empty", "law_staff", "inventory") {
    val charges = Degrade.charges(player, player.inventory, slot)
    if (charges == 0) {
        player.message("The staff has no charges for your to remove.")
    } else {
        val success = player.inventory.transaction {
            val added = addToLimit("law_rune", charges)
            if (added <= 0) {
                error = TransactionError.Deficient(charges)
            } else {
                Degrade.discharge(player, player.inventory, slot, amount = added)
            }
        }
        if (success) {
            player.message("You remove charges from the staff and retrieve some law runes.")
        } else {
            player.inventoryFull()
        }
    }
}

itemOnItem("law_rune", "law_staff", "inventory") { player ->
    val spaces = 1000 - Degrade.charges(player, player.inventory, toSlot)
    val count = player.inventory.count(fromItem.id).coerceAtMost(spaces)
    if (count <= 0) {
        player.message("The staff already has the maximum amount of charges.")
    } else if (player.inventory.remove(fromItem.id, count)) {
        player.message("You charge the staff with law runes.")
        Degrade.charge(player, player.inventory, toSlot, amount = count)
    }
}