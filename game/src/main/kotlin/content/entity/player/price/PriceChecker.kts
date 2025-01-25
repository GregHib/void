package content.entity.player.price

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.engine.inv.moveAll
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.world.activity.bank.isNote
import world.gregs.voidps.world.activity.bank.noted
import world.gregs.voidps.world.community.trade.offer
import content.entity.player.dialogue.type.intEntry
import content.entity.player.modal.Tab

/*
    Price checker interface
 */
interfaceOpen("price_checker") { player ->
    player.interfaceOptions.unlockAll(id, "items", 0 until 28)
    player["price_checker_total"] = 0
    player["price_checker_limit"] = Int.MAX_VALUE
    player.open("price_checker_side")
}

interfaceOption("Remove-*", "items", "price_checker") {
    val amount = when (option) {
        "Remove-1" -> 1
        "Remove-5" -> 5
        "Remove-10" -> 10
        "Remove-All" -> player.offer.count(item.id)
        "Remove-X" -> intEntry("Enter amount:")
        else -> return@interfaceOption
    }
    player.offer.transaction {
        moveToLimit(item.id, amount, player.inventory)
    }
    when (player.offer.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        else -> {}
    }
}

interfaceClose("price_checker") { player ->
    player.close("price_checker_side")
    player.sendScript("clear_dialogues")
    player.offer.moveAll(player.inventory)
}

/*
    Price checker inventory interface
 */

interfaceOpen("price_checker_side") { player ->
    player["tab"] = Tab.Inventory.name
    player.interfaceOptions.send(id, "items")
    player.interfaceOptions.unlockAll(id, "items", 0 until 28)
    player.sendInventory(player.inventory)
}

interfaceOption("Add*", "items", "price_checker_side") {
    val amount = when (option) {
        "Add" -> 1
        "Add-5" -> 5
        "Add-10" -> 10
        "Add-All" -> player.inventory.count(item.id)
        "Add-X" -> intEntry("Enter amount:")
        else -> return@interfaceOption
    }
    player.inventory.transaction {
        moveToLimit(item.id, amount, player.offer)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Invalid -> player.message("That item is not tradeable.")
        else -> {}
    }
}

interfaceClose("price_checker_side") { player ->
    player.open("inventory")
}

itemChange("trade_offer") { player ->
    var total = 0L
    for (index in player.offer.indices) {
        val item = player.offer[index]
        if (item.isEmpty()) {
            continue
        }
        val notNoted = if (item.isNote) item.noted ?: item else item
        val price = notNoted.def["price", notNoted.def.cost]
        player["value_$index"] = price
        total += price * item.amount
    }
    player["price_checker_total"] = total.toInt()
}