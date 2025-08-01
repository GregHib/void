package content.social.trade.exchange

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.dialogue.continueItemDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import kotlin.math.ceil

val exchange: GrandExchange by inject()

interfaceOpen("grand_exchange") { player ->
    player["grand_exchange_page"] = "offers"
    for (id in player["grand_exchange_offers", LongArray(6)]) {

    }
}

interfaceOption("Make Buy Offer", "buy_*", "grand_exchange") {
    val slot = component.removePrefix("buy_offer_").toInt()
    player["grand_exchange_box"] = slot
    player["grand_exchange_page"] = "buy"
    player["grand_exchange_item_id"] = -1
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

interfaceOption("Offer", "items", "stock_side") {
    player["grand_exchange_item"] = item.id
    player["grand_exchange_item_id"] = item.def.id
    player["grand_exchange_quantity"] = item.amount
    val price = 500
    player["grand_exchange_price"] = price
    player["grand_exchange_range_min"] = ceil(price * 0.95).toInt()
    player["grand_exchange_range_max"] = ceil(price * 1.05).toInt()
}

interfaceOption("Back", "back", "grand_exchange") {
    player["grand_exchange_box"] = -1
    player["grand_exchange_page"] = "offers"
    player.open("inventory")
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
}