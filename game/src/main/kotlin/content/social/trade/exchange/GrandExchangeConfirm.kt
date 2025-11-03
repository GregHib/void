package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.bank
import content.entity.player.bank.noted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class GrandExchangeConfirm : Script {

    val exchange: GrandExchange by inject()

    val logger = InlineLogger()

    init {
        interfaceOption("Confirm Offer", "grand_exchange:confirm") {
            if (get("grand_exchange_item_id", -1) == -1) {
                // https://youtu.be/wAtBnxSxgiA?si=jsurs070eip_6INS&t=191
                message("You must choose an item first.")
                return@interfaceOption
            }
            val slot: Int = get("grand_exchange_box") ?: return@interfaceOption
            val itemId: String = get("grand_exchange_item") ?: return@interfaceOption
            val amount: Int = get("grand_exchange_quantity") ?: return@interfaceOption
            val price: Int = get("grand_exchange_price") ?: return@interfaceOption
            when (get("grand_exchange_page", "offers")) {
                "buy" -> {
                    val total = price * amount
                    var fromBank = false
                    inventory.transaction {
                        var removed = removeToLimit("coins", total)
                        if (removed < total && Settings["grandExchange.useBankCoins", false]) {
                            val txn = link(bank)
                            removed += txn.removeToLimit("coins", total - removed)
                            fromBank = true
                        }
                        if (removed < total) {
                            error = TransactionError.Deficient(total - removed)
                        }
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> {
                            if (fromBank) {
                                message("Payment has been taken from your bank.")
                            }
                            offers[slot] = exchange.buy(this, Item(itemId, amount), price)
                        }
                        is TransactionError.Deficient -> {
                            notEnough("coins")
                            return@interfaceOption
                        }
                        else -> return@interfaceOption
                    }
                }
                "sell" -> {
                    var removed = 0
                    inventory.transaction {
                        removed += removeToLimit(itemId, amount)
                        if (removed < amount) {
                            val noted = Item(itemId, amount).noted?.id ?: itemId
                            removed += removeToLimit(noted, amount - removed)
                        }

                        if (removed < amount) {
                            error = TransactionError.Deficient(amount - removed)
                        }
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> offers[slot] = exchange.sell(this, Item(itemId, amount), price)
                        else -> {
                            logger.warn { "Error removing GE items $name ${inventory.transaction.error} $slot $itemId $amount $price" }
                            return@interfaceOption
                        }
                    }
                }
                else -> return@interfaceOption
            }

            inventories.inventory("collection_box_$slot").clear()
            exchange.refresh(this, slot)
            GrandExchange.clearSelection(this)
        }

        interfaceOption("Back", "grand_exchange:back") {
            val box: Int = get("grand_exchange_box", -1)
            val collectionBox = inventories.inventory("collection_box_$box")
            if (collectionBox.isEmpty()) {
                val offer = offers.getOrNull(box)
                if (offer != null && offer.state.cancelled) {
                    offers[box] = ExchangeOffer.EMPTY
                    exchange.offers.remove(offer)
                    exchange.refresh(this, box)
                }
            }
            GrandExchange.clearSelection(this)
        }
    }
}
