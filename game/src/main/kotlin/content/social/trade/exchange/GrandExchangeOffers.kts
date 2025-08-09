package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.dialogue.type.intEntry
import content.entity.player.inv.item.tradeable
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.continueItemDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import kotlin.math.ceil

val exchange: GrandExchange by inject()
val itemDefinitions: ItemDefinitions by inject()

playerSpawn { player ->
    exchange.login(player)
}

interfaceOpen("grand_exchange") { player ->
    player.sendVariable("grand_exchange_ranges")
    player["grand_exchange_page"] = "offers"
    player["grand_exchange_box"] = -1
    player.interfaceOptions.unlockAll(id, "collect_slot_0")
    player.interfaceOptions.unlockAll(id, "collect_slot_1")
    exchange.refresh(player)
}

interfaceOption("Collect*", "collect_slot_*", "grand_exchange") {
    val index = component.removePrefix("collect_slot_").toInt()
    val box: Int = player["grand_exchange_box"] ?: return@interfaceOption
    val id: Int = player["grand_exchange_offer_${box}"] ?: return@interfaceOption
    val collectionBox = player.inventories.inventory("collection_box_${box}")
    val item = collectionBox[index]
    var noted = item
    if (option == "Collect_notes") {
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
            val offer = exchange.offers.offer(id)
            if (offer != null && offer.state.cancelled) {
                exchange.offers.remove(id)
                player.clear("grand_exchange_offer_${box}")
                clear()
            }
            exchange.refresh(player, box)
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

interfaceOption("Make Offer", "view_offer_*", "grand_exchange") {
    val slot = component.removePrefix("view_offer_").toInt()
    val id: Int = player["grand_exchange_offer_${slot}"] ?: return@interfaceOption
    val offer = exchange.offers.offer(id) ?: return@interfaceOption
    player["grand_exchange_box"] = slot
    selectItem(player, offer.item)
}

interfaceOption("Make Buy Offer", "buy_offer_*", "grand_exchange") {
    val slot = component.removePrefix("buy_offer_").toInt()
    player["grand_exchange_box"] = slot
    player["grand_exchange_page"] = "buy"
    player["grand_exchange_item_id"] = -1
    openItemSearch(player)
}

interfaceOption("Choose Item", "choose_item", "grand_exchange") {
    openItemSearch(player)
}

fun openItemSearch(player: Player) {
    player.open("grand_exchange_item_dialog")
    player.sendScript("item_dialogue_reset", "Grand Exchange Item Search")
}

interfaceOption("Make Sell Offer", "sell_offer_*", "grand_exchange") {
    val slot = component.removePrefix("sell_offer_").toInt()
    player["grand_exchange_box"] = slot
    player["grand_exchange_page"] = "sell"
    player.open("stock_side")
    player["grand_exchange_item_id"] = -1
}

interfaceOpen("stock_side") { player ->
    player.tab(Tab.Inventory)
    player.interfaceOptions.send(id, "items")
    player.interfaceOptions.unlockAll(id, "items", 0 until 28)
    player.sendInventory(player.inventory)
    player.sendScript("grand_exchange_hide_all")
}

/*
 * https://youtu.be/3ussM7P1j00?si=IHR8ZXl2kN0bjIfx&t=398
 * "One or more of your Grand Exchange offers have been updated."
 *
 * "Abort request acknowledged. Please be aware that your offer may have already been completed."
 *
 */

val logger = InlineLogger()

interfaceOption("Offer", "items", "stock_side") {
    val item = if (item.isNote) item.noted else item
    if (item == null) {
        logger.warn { "Issue selling noted item on GE: ${this.item}" }
        return@interfaceOption
    }
    if (!item.tradeable) {
        player.message("This item can't be traded on the grand exchange.") // TODO proper message
        return@interfaceOption
    }
    selectItem(player, item.id)
    player["grand_exchange_quantity"] = item.amount
    player["grand_exchange_price"] = player["grand_exchange_market_price", 0]
}

interfaceOption("Back", "back", "grand_exchange") {
    clear()
}

interfaceOption("Confirm Offer", "confirm", "grand_exchange") {
    val slot: Int = player["grand_exchange_box"] ?: return@interfaceOption
    val itemId: String = player["grand_exchange_item"] ?: return@interfaceOption
    val amount: Int = player["grand_exchange_quantity"] ?: return@interfaceOption
    val price: Int = player["grand_exchange_price"] ?: return@interfaceOption
    val id = when (player["grand_exchange_page", "offers"]) {
        "buy" -> {
            // TODO take from bank
            if (!player.inventory.remove("coins", price * amount)) {
                player.message("Not enough coins") // TODO proper message
                return@interfaceOption
            }
            exchange.buy(player, Item(itemId, amount), price)
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
                is TransactionError.Deficient -> {
                    player.message("Not enough items") // TODO proper message
                    return@interfaceOption
                }
                else -> {
                    logger.warn { "Error removing GE items ${player.inventory.transaction.error}" }
                    return@interfaceOption
                }
            }
        }
        else -> return@interfaceOption
    }
    player.inventories.inventory("collection_box_${slot}").clear()
    player["grand_exchange_offer_${slot}"] = id
    exchange.refresh(player, slot)
    clear()
}

fun InterfaceOption.clear() {
    player["grand_exchange_box"] = -1
    player["grand_exchange_page"] = "offers"
    player.sendScript("item_dialogue_close")
    player.close("stock_side")
    player.close("item_info")
    player.clear("grand_exchange_item")
    player.clear("grand_exchange_item_id")
    player.clear("grand_exchange_price")
    player.clear("grand_exchange_market_price")
    player.clear("grand_exchange_range_min")
    player.clear("grand_exchange_range_max")
    player.clear("grand_exchange_quantity")
}

interfaceOption("Add *", "add_*", "grand_exchange") {
    when (player["grand_exchange_page", "offers"]) {
        "sell" -> player["grand_exchange_quantity"] = when (component) {
            "add_1" -> 1
            "add_10" -> 10
            "add_100" -> 100
            "add_all" -> {
                val item = Item(player["grand_exchange_item", ""])
                val noted = item.noted
                var total = 0
                if (noted != null) {
                    total += player.inventory.count(noted.id)
                }
                total += player.inventory.count(item.id)
                total
            }
            else -> return@interfaceOption
        }
        "buy" -> when (component) {
            "add_1" -> player.inc("grand_exchange_quantity", 1)
            "add_10" -> player.inc("grand_exchange_quantity", 10)
            "add_100" -> player.inc("grand_exchange_quantity", 100)
            "add_all" -> player.inc("grand_exchange_quantity", 1000)
            else -> return@interfaceOption
        }
    }
}

interfaceOption("Edit Quantity", "add_x", "grand_exchange") {
    player["grand_exchange_quantity"] = when (player["grand_exchange_page", "offers"]) {
        "sell" -> intEntry("Enter the amount you wish to sell:")
        "buy" -> intEntry("Enter the amount you wish to purchase:")
        else -> return@interfaceOption
    }
}

interfaceOption("Increase Quantity", "increase_quantity", "grand_exchange") {
    if (player["grand_exchange_quantity", 0] < Int.MAX_VALUE - 1) {
        player.inc("grand_exchange_quantity", 1)
    }
}

interfaceOption("Decrease Quantity", "decrease_quantity", "grand_exchange") {
    if (player.dec("grand_exchange_quantity", 1) < 0) {
        player["grand_exchange_quantity"] = 0
    }
}

interfaceOption("Decrease Price", "decrease_price", "grand_exchange") {
    if (player.dec("grand_exchange_price", 1) < 0) {
        player["grand_exchange_quantity"] = 0
    }
}

interfaceOption("Increase Price", "increase_price", "grand_exchange") {
    if (player["grand_exchange_price", 0] < Int.MAX_VALUE - 1) {
        player.inc("grand_exchange_price", 1)
    }
}

interfaceOption("Edit Price", "offer_x", "grand_exchange") {
    player["grand_exchange_price"] = when (player["grand_exchange_page", "offers"]) {
        "sell" -> intEntry("Enter the price you wish to sell for:")
        "buy" -> intEntry("Enter the price you wish to buy for:")
        else -> return@interfaceOption
    }.coerceIn(player["grand_exchange_range_min", 0], player["grand_exchange_range_max", 0])
}

// TODO: Toggle to replace min/max with +/- 5%

interfaceOption("Offer Minimum Price", "offer_min", "grand_exchange") {
    player["grand_exchange_price"] = player["grand_exchange_range_min", 0]
}

interfaceOption("Offer Maximum Price", "offer_max", "grand_exchange") {
    player["grand_exchange_price"] = player["grand_exchange_range_max", 0]
}

continueItemDialogue { player ->
    selectItem(player, item)
    player["grand_exchange_price"] = player["grand_exchange_market_price", 0]
    ItemInfo.showInfo(player, Item(item))
}

fun selectItem(player: Player, item: String) {
    val definition = itemDefinitions.get(item)
    player["grand_exchange_item"] = item
    player["grand_exchange_item_id"] = definition.id
    player.interfaces.sendText("grand_exchange", "examine", definition["examine", ""])
    val price = exchange.history.marketPrice(item)
    player["grand_exchange_market_price"] = price
    player["grand_exchange_range_min"] = ceil(price * 0.95).toInt()
    player["grand_exchange_range_max"] = ceil(price * 1.05).toInt()
}

fun abort(player: Player, slot: Int) {
    val id: Int = player["grand_exchange_offer_${slot}"] ?: return
    exchange.cancel(id)
    player.message("Abort request acknowledged. Please be aware that your offer may have already been completed.")
}