package content.social.trade.lend

import com.github.michaelbull.logging.InlineLogger
import content.social.trade.lend.Loan.getExpiry
import content.social.trade.lend.Loan.returnLoan
import content.social.trade.returnedItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.moveAll

class ItemReturning : Script {

    val logger = InlineLogger()
    val players: Players by inject()

    init {
        interfaceOpened("returned_items") {
            sendInventory(returnedItems)
        }

        interfaceOption("Reclaim", "returned_items:item") {
            if (!contains("lent_item_id")) {
                returnItem(this)
                return@interfaceOption
            }
            if (contains("lend_timeout")) {
                message("Your item will be returned to you ${getExpiry(this, "lend_timeout")}.") // TODO real message
                return@interfaceOption
            }
            if (!contains("lent_to")) {
                logger.warn { "Invalid item lending state; can't force claim an item when target has already logged out." }
                return@interfaceOption
            }

            message("Demanding return of item.")
            val name: String? = get("lent_to")
            val borrower = if (name == null) null else players.get(name)
            if (borrower == null) {
                message("There was an issue returning your item.")
                logger.warn { "Unable to find lent item borrower '$name'." }
                return@interfaceOption
            }

            softTimers.clear("loan_message")
            clear("lent_item_id")
            clear("lent_item_amount")
            returnLoan(borrower)
            returnItem(this)
            message("Your item has been returned.")
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
}
