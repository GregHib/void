package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.noted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.exchange.ExchangeHistory
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class GrandExchangeCollection(val exchange: GrandExchange) : Script {

    val logger = InlineLogger()

    init {
        interfaceOption(id = "grand_exchange:collect_slot_*") {
            if (!it.option.startsWith("Collect")) {
                return@interfaceOption
            }
            val index = it.component.removePrefix("collect_slot_").toInt()
            val box: Int = get("grand_exchange_box") ?: return@interfaceOption
            collect(it.option, box, index)
        }

        interfaceOption(id = "collection_box:collection_box_*") {
            if (!it.option.startsWith("Collect")) {
                return@interfaceOption
            }
            val box = it.component.removePrefix("collection_box_").toInt()
            val index = if (it.itemSlot == 2) 1 else 0
            collect(it.option, box, index)
        }

        interfaceOption("Abort Offer", "grand_exchange:offer_abort") {
            val slot: Int = get("grand_exchange_box") ?: return@interfaceOption
            abort(slot)
        }

        interfaceOption("Abort Offer", "grand_exchange:view_offer_*") {
            val slot = it.component.removePrefix("view_offer_").toInt()
            if (slot > 1 && !World.members) {
                return@interfaceOption
            }
            abort(slot)
        }
    }

    fun Player.collect(option: String, box: Int, index: Int) {
        val offer = offers.getOrNull(box) ?: return
        val collectionBox = inventories.inventory("collection_box_$box")
        val item = collectionBox[index]
        var noted = item
        // Option 1 is to collect noted if amount > 1 otherwise options flip
        if ((item.amount > 1 && option == "Collect_notes") || (item.amount == 1 && option == "Collect")) {
            noted = item.noted ?: item
        }
        var added = 0
        inventory.transaction {
            val txn = link(collectionBox)
            added = addToLimit(noted.id, item.amount)
            if (added < 1) {
                error = TransactionError.Full()
            }
            txn.remove(item.id, added)
        }
        when (inventory.transaction.error) {
            is TransactionError.Full -> inventoryFull()
            TransactionError.None -> {
                val actual = Item(item.id, added)
                AuditLog.event(this, "claimed", actual)
                if (collectionBox.isEmpty()) {
                    if (offer.state.cancelled) {
                        if (offer.completed > 0) {
                            history.add(0, ExchangeHistory(offer))
                        }
                        offers[box] = ExchangeOffer.EMPTY
                        exchange.offers.remove(offer)
                        GrandExchange.clearSelection(this)
                    }
                    exchange.refresh(this, box)
                } else if (collectionBox.contains(item.id)) {
                    inventoryFull()
                }
            }
            else -> logger.warn { "Issue collecting items from grand exchange ${inventory.transaction.error} $name $item $index" }
        }
    }

    fun Player.abort(slot: Int) {
        exchange.cancel(this, slot)
        // https://youtu.be/3ussM7P1j00?si=IHR8ZXl2kN0bjIfx&t=398
        message("Abort request acknowledged. Please be aware that your offer may have already been completed.")
    }
}
