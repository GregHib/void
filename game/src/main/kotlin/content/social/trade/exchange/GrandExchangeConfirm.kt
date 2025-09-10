package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.bank
import content.entity.player.bank.noted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

@Script
class GrandExchangeConfirm {

    val exchange: GrandExchange by inject()

    val logger = InlineLogger()

    init {
        interfaceOption("Confirm Offer", "confirm", "grand_exchange") {
            if (player["grand_exchange_item_id", -1] == -1) {
                // https://youtu.be/wAtBnxSxgiA?si=jsurs070eip_6INS&t=191
                player.message("You must choose an item first.")
                return@interfaceOption
            }
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
                            player.offers[slot] = exchange.buy(player, Item(itemId, amount), price)
                        }
                        is TransactionError.Deficient -> {
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
                        TransactionError.None -> player.offers[slot] = exchange.sell(player, Item(itemId, amount), price)
                        else -> {
                            logger.warn { "Error removing GE items ${player.name} ${player.inventory.transaction.error} $slot $itemId $amount $price" }
                            return@interfaceOption
                        }
                    }
                }
                else -> return@interfaceOption
            }

            player.inventories.inventory("collection_box_$slot").clear()
            exchange.refresh(player, slot)
            GrandExchange.clearSelection(player)
        }

        interfaceOption("Back", "back", "grand_exchange") {
            val box: Int = player["grand_exchange_box", -1]
            val collectionBox = player.inventories.inventory("collection_box_$box")
            if (collectionBox.isEmpty()) {
                val offer = player.offers.getOrNull(box)
                if (offer != null && offer.state.cancelled) {
                    player.offers[box] = ExchangeOffer.EMPTY
                    exchange.offers.remove(offer)
                    exchange.refresh(player, box)
                }
            }
            GrandExchange.clearSelection(player)
        }
    }
}
