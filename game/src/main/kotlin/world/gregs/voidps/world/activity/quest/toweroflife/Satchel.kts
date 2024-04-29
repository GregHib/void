package world.gregs.voidps.world.activity.quest.toweroflife

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

val cake = 0x1
val banana = 0x2
val sandwich = 0x4

inventoryItem("Inspect", "*_satchel") {
    val charges = player.inventory.charges(player, slot)
    val hasCake = charges and cake != 0
    val hasBanana = charges and banana != 0
    val hasSandwich = charges and sandwich != 0
    // TODO proper messages
    if (hasCake || hasBanana || hasSandwich) {
        statement("The satchel contains ${hasCake.toInt()} cake, ${hasBanana.toInt()} banana and ${hasSandwich.toInt()} sandwich.")
    } else {
        player.message("The satchel is empty.")
    }
}

inventoryItem("Empty", "*_satchel") {
    var charges = player.inventory.charges(player, slot)
    player.inventory.transaction {
        if (charges and cake != 0) {
            add("cake")
            charges = charges and cake.inv()
            set(slot, item.copy(amount = charges))
        }
        if (!failed && charges and banana != 0 && inventory.spaces > 0) {
            add("banana")
            charges = charges and banana.inv()
            set(slot, item.copy(amount = charges))
        }
        if (!failed && charges and sandwich != 0 && inventory.spaces > 0) {
            add("triangle_sandwich")
            charges = charges and sandwich.inv()
            set(slot, item.copy(amount = charges))
        }
    }
}

itemOnItem("cake", "*_satchel") { player ->
    val charges = player.inventory.charges(player, toSlot)
    if (charges and cake != 0) {
        player.message("The satchel already contains cake.") // TODO proper message
        return@itemOnItem
    }
    player.inventory.transaction {
        remove(fromSlot, "cake")
        set(toSlot, toItem.copy(amount = charges + cake))
    }
}

itemOnItem("banana", "*_satchel") { player ->
    val charges = player.inventory.charges(player, toSlot)
    if (charges and banana != 0) {
        player.message("The satchel already contains a banana.") // TODO proper message
        return@itemOnItem
    }
    player.inventory.transaction {
        remove(fromSlot, "banana")
        set(toSlot, toItem.copy(amount = charges + banana))
    }
}

itemOnItem("triangle_sandwich", "*_satchel") { player ->
    val charges = player.inventory.charges(player, toSlot)
    if (charges and sandwich != 0) {
        player.message("The satchel already contains a sandwich.") // TODO proper message
        return@itemOnItem
    }
    player.inventory.transaction {
        remove(fromSlot, "triangle_sandwich")
        set(toSlot, toItem.copy(amount = charges + sandwich))
    }
}
