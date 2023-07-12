package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.community.trade.lend.Loan.getExpiry
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.community.trade.returnedItems

val logger = InlineLogger()
val players: Players by inject()

on<InterfaceOpened>({ id == "returned_items" }) { player: Player ->
    player.sendInventory(player.returnedItems)
}

on<InterfaceOption>({ id == "returned_items" && component == "item" && option == "Reclaim" }) { player: Player ->
    if (player.contains("lent_item_id")) {
        if (player.contains("lend_timeout")) {
            player.message("Your item will be returned to you ${getExpiry(player, "lend_timeout")}.") // TODO real message
        } else if(player.contains("lent_to")) {
            player.message("Demanding return of item.")
            val name = player.get<String>("lent_to")
            val borrower = players.get(name)
            if (borrower != null) {
                player.softTimers.clear("loan_message")
                player.clear("lent_item_id")
                player.clear("lent_item_amount")
                returnLoan(borrower)
                returnItem(player)
                player.message("Your item has been returned.")
            } else {
                player.message("There was an issue returning your item.")
                logger.warn { "Unable to find lent item borrower '$name'." }
            }
        } else {
            logger.warn { "Invalid item lending state; can't force claim an item when target has already logged out." }
        }
    } else {
        returnItem(player)
    }
}

fun returnItem(player: Player) {
    player.returnedItems.transaction {
        moveAll(player.inventory)
    }
    when (player.returnedItems.transaction.error) {
        TransactionError.Invalid -> logger.info { "Return item issue: $player" }
        is TransactionError.Full -> player.inventoryFull()
        else -> player.clear("lent_to")
    }
}