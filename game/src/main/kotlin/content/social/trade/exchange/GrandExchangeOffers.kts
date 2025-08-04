package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.network.login.protocol.encode.grandExchange
import kotlin.math.ceil

val exchange: GrandExchange by inject()

interfaceOpen("grand_exchange") { player ->
    player["grand_exchange_page"] = "offers"
    player["grand_exchange_box"] = -1
    player.interfaceOptions.unlockAll(id, "collect_slot_0")
    player.interfaceOptions.unlockAll(id, "collect_slot_1")
    for (i in 0 until 6) {
//        if(i == 0) {
        // TODO send unlocks
        if (i != 5) {
            val inv = player.inventories.inventory("collection_box_${i}")
            inv.transaction {
                set(1, Item("coins", 999_999))
                set(0, Item("abyssal_whip", 1))
            }
            player.client?.grandExchange(i, 2, 4151, 1_000_000, 2, 1, 999_000)
        }
//        } else {
//            player.client?.grandExchange(i)
//        }
    }

//    for (id in player["grand_exchange_offers", LongArray(6)]) {
//
//    }
}

interfaceOption("Collect*", "collect_slot_*", "grand_exchange") {
    val index = component.removePrefix("collect_slot_").toInt()
    val box: Int = player["grand_exchange_box"] ?: return@interfaceOption
    val collectionBox = player.inventories.inventory("collection_box_${box}")
    var item = collectionBox[index]
    if (option == "Collect_notes") {
        item = item.noted ?: item
    }
    player.inventory.transaction {
        val txn = link(collectionBox)
        txn.clear(index)
        add(item.id, item.amount)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.None -> if (collectionBox.isEmpty()) {
            player.client?.grandExchange(box, 0)
        }
        else -> logger.warn { "Issue collecting items from grand exchange ${player.inventory.transaction.error} ${player.name} $item $index" }
    }
}

interfaceOption("Abort Offer", "offer_abort", "grand_exchange") {
    val slot: Int = player["grand_exchange_box"] ?: return@interfaceOption
    player.client?.grandExchange(slot, 5, 4151, 1_000_000, 2, 1, 1_000_000)
}

interfaceOption("Abort Offer", "view_offer_*", "grand_exchange") {
    val slot = component.removePrefix("view_offer_").toInt()
    player.client?.grandExchange(slot, 5, 4151, 1_000_000, 2, 1, 1_000_000)
}

interfaceOption("Make Offer", "view_offer_*", "grand_exchange") {
    val slot = component.removePrefix("view_offer_").toInt()
    player["grand_exchange_box"] = slot
    player["grand_exchange_item_id"] = 4151
    player["grand_exchange_item"] = "abyssal_whip"
    player.interfaces.sendText("grand_exchange", "examine", "Whip me baby one more time.")
    player["grand_exchange_market_price"] = 950_000
    player["grand_exchange_range_min"] = 900_000
    player["grand_exchange_range_max"] = 1_000_000
}

interfaceOption("Make Buy Offer", "buy_*", "grand_exchange") {
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

interfaceOption("Make Sell Offer", "sell_*", "grand_exchange") {
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
    player["grand_exchange_item"] = item.id
    player["grand_exchange_item_id"] = item.def.id
    player["grand_exchange_quantity"] = item.amount
    val price = 500
    player["grand_exchange_price"] = price
    player["grand_exchange_market_price"] = price
    player["grand_exchange_range_min"] = ceil(price * 0.95).toInt()
    player["grand_exchange_range_max"] = ceil(price * 1.05).toInt()
    player.interfaces.sendText("grand_exchange", "examine", item.def["examine", ""])
}

interfaceOption("Back", "back", "grand_exchange") {
    back()
}

interfaceOption("Confirm Offer", "confirm", "grand_exchange") {
    back()
}

fun InterfaceOption.back() {
    player["grand_exchange_box"] = -1
    player["grand_exchange_page"] = "offers"
    player.sendScript("item_dialogue_close")
    player.close("stock_side")
    player.close("item_info")
}

interfaceOption("Add *", "add_*", "grand_exchange") {
    when (player["grand_exchange_page", "offers"]) {
        "sell" -> player["grand_exchange_quantity"] = when (component) {
            "add_1" -> 1
            "add_10" -> 10
            "add_100" -> 100
            "add_all" -> player.inventory.count(player["grand_exchange_item", ""])
            else -> -1
        }
        "buy" -> when (component) {
            "add_1" -> player.inc("grand_exchange_quantity", 1)
            "add_10" -> player.inc("grand_exchange_quantity", 10)
            "add_100" -> player.inc("grand_exchange_quantity", 100)
            "add_all" -> player.inc("grand_exchange_quantity", 1000)
            "add_x" -> {}
        }
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

// TODO: Toggle to replace min/max with +/- 5%

interfaceOption("Offer Minimum Price", "offer_min", "grand_exchange") {
    player["grand_exchange_price"] = player["grand_exchange_range_min", 0]
}

interfaceOption("Offer Maximum Price", "offer_max", "grand_exchange") {
    player["grand_exchange_price"] = player["grand_exchange_range_max", 0]
}

continueItemDialogue { player ->
    player["grand_exchange_item"] = item
    player["grand_exchange_item_id"] = def.id
    val price = def.cost
    player["grand_exchange_price"] = price
    player["grand_exchange_market_price"] = price
    player.interfaces.sendText("grand_exchange", "examine", def["examine", ""])
    player["grand_exchange_range_min"] = ceil(price * 0.95).toInt()
    player["grand_exchange_range_max"] = ceil(price * 1.05).toInt()
    ItemInfo.showInfo(player, Item(item))
}