package content.social.trade.exchange

import content.entity.player.bank.noted
import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import kotlin.math.ceil

class GrandExchangeControls : Script {

    init {
        interfaceOpened("grand_exchange") { id ->
            /*
                This is a hacky way of converting between original and newer ui (limited price range vs unlimited with +/-5%)
                It doesn't account for hover and tooltip changes or location of buttons and isn't the most responsive as
                it's limited by the speed variables change by the existing (old) cs2.
             */
            val limit = Settings["grandExchange.priceLimit", true]
            interfaces.sendVisibility(id, "price_range_min", limit)
            interfaces.sendVisibility(id, "price_range_max", limit)
            interfaces.sendVisibility(id, "price_range", limit)
            interfaces.sendVisibility(id, "offer_min_sprite", limit)
            interfaces.sendVisibility(id, "offer_max_sprite", limit)
            interfaces.sendText(id, "offer_min", if (limit) "" else "-5%")
            interfaces.sendText(id, "offer_max", if (limit) "" else "+5%")
        }

        interfaceOption(id = "grand_exchange:add_*") {
            if (!itemSelected() || !it.option.startsWith("Add")) {
                return@interfaceOption
            }
            closeDialogue()
            when (get("grand_exchange_page", "offers")) {
                "sell" -> {
                    val total = totalItems()
                    set(
                        "grand_exchange_quantity",
                        when (it.component) {
                            "add_1" -> 1
                            "add_10" -> 10
                            "add_100" -> 100
                            "add_all" -> total
                            else -> return@interfaceOption
                        }.coerceAtMost(total),
                    )
                }
                "buy" -> when (it.component) {
                    "add_1" -> inc("grand_exchange_quantity", 1)
                    "add_10" -> inc("grand_exchange_quantity", 10)
                    "add_100" -> inc("grand_exchange_quantity", 100)
                    "add_all" -> inc("grand_exchange_quantity", 1000)
                    else -> return@interfaceOption
                }
            }
        }

        interfaceOption("Edit Quantity", "grand_exchange:add_x") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            set(
                "grand_exchange_quantity",
                when (get("grand_exchange_page", "offers")) {
                    "sell" -> intEntry("Enter the amount you wish to sell:").coerceAtMost(totalItems())
                    "buy" -> intEntry("Enter the amount you wish to purchase:")
                    else -> return@interfaceOption
                },
            )
        }

        interfaceOption("Increase Quantity", "grand_exchange:increase_quantity") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            if (get("grand_exchange_quantity", 0) < Int.MAX_VALUE - 1) {
                set(
                    "grand_exchange_quantity",
                    (get("grand_exchange_quantity", 0) + 1).coerceAtMost(
                        when (get("grand_exchange_page", "offers")) {
                            "sell" -> totalItems()
                            "buy" -> Int.MAX_VALUE
                            else -> return@interfaceOption
                        },
                    ),
                )
            }
        }

        interfaceOption("Decrease Quantity", "grand_exchange:decrease_quantity") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            if (dec("grand_exchange_quantity", 1) < 0) {
                set("grand_exchange_quantity", 0)
            }
        }

        interfaceOption("Increase Price", "grand_exchange:increase_price") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            val limit = if (Settings["grandExchange.priceLimit", true]) get("grand_exchange_range_max", 0) else Int.MAX_VALUE
            set("grand_exchange_price", (get("grand_exchange_price", 0) + 1L).coerceAtMost(limit.toLong()).toInt())
            if (!Settings["grandExchange.priceLimit", true]) {
                updateLimits(this)
            }
        }

        interfaceOption("Decrease Price", "grand_exchange:decrease_price") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            val limit = if (Settings["grandExchange.priceLimit", true]) get("grand_exchange_range_min", 0) else 0
            set("grand_exchange_price", (get("grand_exchange_price", 0) - 1).coerceAtLeast(limit))
            if (!Settings["grandExchange.priceLimit", true]) {
                updateLimits(this)
            }
        }

        interfaceOption("Offer Market Price", "grand_exchange:offer_market") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            if (!Settings["grandExchange.priceLimit", true] && hasClock("grand_exchange_price_delay")) {
                return@interfaceOption
            }
            start("grand_exchange_price_delay", 1)
            set("grand_exchange_price", get("grand_exchange_market_price", 0))
            if (!Settings["grandExchange.priceLimit", true]) {
                updateLimits(this)
            }
        }

        interfaceOption("Edit Price", "grand_exchange:offer_x") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            val min = if (Settings["grandExchange.priceLimit", true]) get("grand_exchange_range_min", 0) else 0
            val max = if (Settings["grandExchange.priceLimit", true]) get("grand_exchange_range_max", 0) else Int.MAX_VALUE
            set(
                "grand_exchange_price",
                when (get("grand_exchange_page", "offers")) {
                    "sell" -> intEntry("Enter the price you wish to sell for:")
                    "buy" -> intEntry("Enter the price you wish to buy for:")
                    else -> return@interfaceOption
                }.coerceIn(min, max),
            )
            if (!Settings["grandExchange.priceLimit", true]) {
                updateLimits(this)
            }
        }

        interfaceOption("Offer Minimum Price", "grand_exchange:offer_min") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            if (Settings["grandExchange.priceLimit", true]) {
                set("grand_exchange_price", get("grand_exchange_range_min", 0))
            } else {
                if (hasClock("grand_exchange_price_delay")) {
                    return@interfaceOption
                }
                start("grand_exchange_price_delay", 1)
                val price = get("grand_exchange_market_price", 0)
                val percent = ceil(price * 0.05).toInt()
                set("grand_exchange_price", (get("grand_exchange_price", 0) - percent).coerceAtLeast(0))
                updateLimits(this)
            }
        }

        interfaceOption("Offer Maximum Price", "grand_exchange:offer_max") {
            if (!itemSelected()) {
                return@interfaceOption
            }
            closeDialogue()
            if (Settings["grandExchange.priceLimit", true]) {
                set("grand_exchange_price", get("grand_exchange_range_max", 0))
            } else {
                if (hasClock("grand_exchange_price_delay")) {
                    return@interfaceOption
                }
                start("grand_exchange_price_delay", 1)
                val price = get("grand_exchange_market_price", 0)
                val percent = ceil(price * 0.05).toInt()
                set("grand_exchange_price", (get("grand_exchange_price", 0) + percent).coerceAtMost(Int.MAX_VALUE))
                updateLimits(this)
            }
        }
    }

    fun Player.totalItems(): Int {
        val item = Item(get("grand_exchange_item", ""))
        val noted = item.noted
        var total = 0
        if (noted != null) {
            total += inventory.count(noted.id)
        }
        total += inventory.count(item.id)
        return total
    }

    /**
     * We have to update the min and max range everytime the price changes to avoid the price conflicting with cs2
     */
    fun updateLimits(player: Player) {
        val price = player["grand_exchange_market_price", 0]
        val percent = ceil(price * 0.05).toInt()
        player["grand_exchange_range_min"] = (player["grand_exchange_price", 0] - percent).coerceAtLeast(0)
        player["grand_exchange_range_max"] = (player["grand_exchange_price", 0] + percent).coerceAtMost(Int.MAX_VALUE)
    }

    fun Player.itemSelected(): Boolean {
        if (get("grand_exchange_item_id", -1) == -1) {
            // https://youtu.be/wAtBnxSxgiA?si=jsurs070eip_6INS&t=191
            message("You must choose an item first.")
            return false
        }
        return true
    }
}
