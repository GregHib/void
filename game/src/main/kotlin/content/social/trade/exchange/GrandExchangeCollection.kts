package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.noted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

val exchange: GrandExchange by inject()
val logger = InlineLogger()

interfaceOption("Collect*", "collect_slot_*", "grand_exchange") {
    val index = component.removePrefix("collect_slot_").toInt()
    val box: Int = player["grand_exchange_box"] ?: return@interfaceOption
    val offer = player.offers.getOrNull(box) ?: return@interfaceOption
    val collectionBox = player.inventories.inventory("collection_box_${box}")
    val item = collectionBox[index]
    var noted = item
    // Option 1 is to collect noted if amount > 1 otherwise options flip
    if ((item.amount > 1 && option == "Collect_notes") || (item.amount == 1 && option == "Collect")) {
        noted = item.noted ?: item
    }
    player.inventory.transaction {
        val txn = link(collectionBox)
        val added = addToLimit(noted.id, item.amount)
        txn.remove(item.id, added)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.None -> if (collectionBox.isEmpty()) {
            if (offer.state.cancelled) {
                exchange.offers.remove(offer)
                player.clear("grand_exchange_offer_${box}")
                GrandExchange.clear(player)
            }
            exchange.refresh(player, box)
        } else if (collectionBox.contains(item.id)) {
            player.inventoryFull()
        }
        else -> logger.warn { "Issue collecting items from grand exchange ${player.inventory.transaction.error} ${player.name} $item $index" }
    }
}

interfaceOption("Abort Offer", "offer_abort", "grand_exchange") {
    val slot: Int = player["grand_exchange_box"] ?: return@interfaceOption
    abort(player, slot)
}

interfaceOption("Abort Offer", "view_offer_*", "grand_exchange") {
    val slot = component.removePrefix("view_offer_").toInt()
    abort(player, slot)
}

fun abort(player: Player, slot: Int) {
    val offer = player.offers.getOrNull(slot) ?: return
    exchange.cancel(player, offer.id)
    // https://youtu.be/3ussM7P1j00?si=IHR8ZXl2kN0bjIfx&t=398
    player.message("Abort request acknowledged. Please be aware that your offer may have already been completed.")
}
