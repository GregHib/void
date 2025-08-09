package content.social.trade.exchange

import content.entity.player.bank.noted
import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory

interfaceOption("Add *", "add_*", "grand_exchange") {
    when (player["grand_exchange_page", "offers"]) {
        "sell" -> {
            val total = totalItems()
            player["grand_exchange_quantity"] = when (component) {
                "add_1" -> 1
                "add_10" -> 10
                "add_100" -> 100
                "add_all" -> total
                else -> return@interfaceOption
            }.coerceAtMost(total)
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
        "sell" -> intEntry("Enter the amount you wish to sell:").coerceAtMost(totalItems())
        "buy" -> intEntry("Enter the amount you wish to purchase:")
        else -> return@interfaceOption
    }
}

interfaceOption("Increase Quantity", "increase_quantity", "grand_exchange") {
    if (player["grand_exchange_quantity", 0] < Int.MAX_VALUE - 1) {
        player["grand_exchange_quantity"] = (player["grand_exchange_quantity", 0] + 1).coerceAtMost(totalItems())
    }
}

interfaceOption("Decrease Quantity", "decrease_quantity", "grand_exchange") {
    if (player.dec("grand_exchange_quantity", 1) < 0) {
        player["grand_exchange_quantity"] = 0
    }
}

interfaceOption("Increase Price", "increase_price", "grand_exchange") {
    player["grand_exchange_price"] = (player["grand_exchange_price", 0] + 1).coerceAtMost(player["grand_exchange_range_max", 0])
}

interfaceOption("Decrease Price", "decrease_price", "grand_exchange") {
    player["grand_exchange_price"] = (player["grand_exchange_price", 0] - 1).coerceAtLeast(player["grand_exchange_range_min", 0])
}

interfaceOption("Offer Market Price", "offer_market", "grand_exchange") {
    player["grand_exchange_price"] = player["grand_exchange_market_price", 0]
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

fun InterfaceOption.totalItems(): Int {
    val item = Item(player["grand_exchange_item", ""])
    val noted = item.noted
    var total = 0
    if (noted != null) {
        total += player.inventory.count(noted.id)
    }
    total += player.inventory.count(item.id)
    return total
}