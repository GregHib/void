package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.bank.bank
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.inv.item.tradeable
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.dialogue.continueItemDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import kotlin.math.ceil

val exchange: GrandExchange by inject()

val logger = InlineLogger()

interfaceOption("Confirm Offer", "confirm", "grand_exchange") {
    val slot: Int = player["grand_exchange_box"] ?: return@interfaceOption
    val itemId: String = player["grand_exchange_item"] ?: return@interfaceOption
    val amount: Int = player["grand_exchange_quantity"] ?: return@interfaceOption
    val price: Int = player["grand_exchange_price"] ?: return@interfaceOption
    val id = when (player["grand_exchange_page", "offers"]) {
        "buy" -> {
            val total = price * amount
            var fromBank = false
            player.inventory.transaction {
                var removed = removeToLimit("coins", total)
                if (removed < total && Settings["grandExchange.useBankCoins", false]) {
                    val txn = link(player.bank)
                    removed += txn.removeToLimit("coins", total - removed)
                    fromBank = true
                }
                if (removed < total) {
                    error = TransactionError.Deficient(total - removed)
                }
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    if (fromBank) {
                        player.message("Payment has been taken from your bank.")
                    }
                    exchange.buy(player, Item(itemId, amount), price)
                }
                is TransactionError.Deficient -> {
                    println(player.inventory.transaction.error)
                    player.notEnough("coins")
                    return@interfaceOption
                }
                else -> return@interfaceOption
            }
        }
        "sell" -> {
            var removed = 0
            player.inventory.transaction {
                removed += removeToLimit(itemId, amount)
                if (removed < amount) {
                    val noted = Item(itemId, amount).noted?.id ?: itemId
                    removed += removeToLimit(noted, amount - removed)
                }

                if (removed < amount) {
                    error = TransactionError.Deficient(amount - removed)
                }
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> exchange.sell(player, Item(itemId, amount), price)
                else -> {
                    logger.warn { "Error removing GE items ${player.name} ${player.inventory.transaction.error} $slot $itemId $amount $price" }
                    return@interfaceOption
                }
            }
        }
        else -> return@interfaceOption
    }
    player.inventories.inventory("collection_box_${slot}").clear()
    player["grand_exchange_offer_${slot}"] = id
    exchange.refresh(player, slot)
    GrandExchange.clear(player)
}

interfaceOption("Back", "back", "grand_exchange") {
    GrandExchange.clear(player)
}