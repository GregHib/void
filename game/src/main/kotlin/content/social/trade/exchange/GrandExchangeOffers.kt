package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.dialogue.continueItemDialogue
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import kotlin.math.ceil

class GrandExchangeOffers : Script {

    val exchange: GrandExchange by inject()
    val itemDefinitions: ItemDefinitions by inject()
    val accountDefinitions: AccountDefinitions by inject()
    val logger = InlineLogger()

    init {
        playerSpawn(exchange::login)

        interfaceOpen("grand_exchange") { id ->
            sendVariable("grand_exchange_ranges")
            set("grand_exchange_page", "offers")
            set("grand_exchange_box", -1)
            interfaceOptions.unlockAll(id, "collect_slot_0")
            interfaceOptions.unlockAll(id, "collect_slot_1")
            for (i in 0 until 6) {
                exchange.refresh(this, i)
            }
        }

        interfaceClose("grand_exchange") {
            GrandExchange.clearSelection(this)
        }

        /*
            Offers
         */

        interfaceOption("Make Offer", "view_offer_*", "grand_exchange") {
            val slot = component.removePrefix("view_offer_").toInt()
            if (slot > 1 && !World.members) {
                return@interfaceOption
            }
            val offer = player.offers.getOrNull(slot) ?: return@interfaceOption
            player["grand_exchange_box"] = slot
            selectItem(player, offer.item)
        }

        /*
            Buy Offer
         */

        interfaceOption("Make Buy Offer", "buy_offer_*", "grand_exchange") {
            val slot = component.removePrefix("buy_offer_").toInt()
            if (slot > 1 && !World.members) {
                return@interfaceOption
            }
            player["grand_exchange_box"] = slot
            player["grand_exchange_page"] = "buy"
            player["grand_exchange_item_id"] = -1
            openItemSearch(player)
        }

        interfaceOption("Choose Item", "choose_item", "grand_exchange") {
            openItemSearch(player)
        }

        continueItemDialogue { player ->
            val def = itemDefinitions.getOrNull(item)
            if (def == null || !def.exchangeable || def.noted || def.lent || def.dummyItem != 0) {
                player.message("You can't trade that item on the Grand Exchange.")
                return@continueItemDialogue
            }
            selectItem(player, item)
            player["grand_exchange_price"] = player["grand_exchange_market_price", 0]
            ItemInfo.showInfo(player, Item(item))
        }

        /*
            Sell Offer
         */

        interfaceOption("Make Sell Offer", "sell_offer_*", "grand_exchange") {
            val slot = component.removePrefix("sell_offer_").toInt()
            if (slot > 1 && !World.members) {
                return@interfaceOption
            }
            player["grand_exchange_box"] = slot
            player["grand_exchange_page"] = "sell"
            player.open("stock_side")
            player["grand_exchange_item_id"] = -1
        }

        interfaceOpen("stock_side") { id ->
            tab(Tab.Inventory)
            interfaceOptions.send(id, "items")
            interfaceOptions.unlockAll(id, "items", 0 until 28)
            sendInventory(inventory)
            sendScript("grand_exchange_hide_all")
        }

        interfaceOption("Offer", "items", "stock_side") {
            val item = if (item.isNote) item.noted else item
            if (item == null) {
                logger.warn { "Issue selling noted item on GE: ${this.item}" }
                return@interfaceOption
            }
            val def = item.def
            if (!def.exchangeable || def.noted || def.lent || def.dummyItem != 0) {
                player.message("You can't trade that item on the Grand Exchange.")
                return@interfaceOption
            }
            selectItem(player, item.id)
            player["grand_exchange_quantity"] = item.amount
            player["grand_exchange_price"] = player["grand_exchange_market_price", 0]
        }

        val grandExchangeItems = itemDefinitions.definitions.filter { def -> def.exchangeable && !def.noted && !def.lent && def.dummyItem == 0 }.map { it.stringId }.toSet()

        adminCommand("offers", stringArg("name", desc = "Item id to search for", autofill = grandExchangeItems), desc = "Search all grand exchange open offers", handler = ::offersCommand)
    }

    fun offersCommand(player: Player, args: List<String>) {
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        player.message("===== Offers =====", ChatType.Console)
        val id = args[0].lowercase()
        val definition = itemDefinitions.getOrNull(id)
        if (definition == null) {
            player.message("No results found for '$id'", ChatType.Console)
            return
        }
        val buying = exchange.offers.buying(id)
        val foundBuy = buying.values.sumOf { it.size }
        player.message("[$id] market price: ${exchange.history.marketPrice(id).toDigitGroupString()}", ChatType.Console)
        if (buying.isNotEmpty()) {
            player.message("$foundBuy buy offers found.", ChatType.Console)
            val price = buying.higherKey(0)
            val highest = buying[price]!!
            for (offer in highest) {
                val user = accountDefinitions.getByAccount(offer.account)?.displayName
                player.message("[$id] - price: $price amount: ${offer.remaining} player: $user", ChatType.Console)
            }
        }
        val selling = exchange.offers.selling(id)
        val foundSell = buying.values.sumOf { it.size }
        if (selling.isNotEmpty()) {
            player.message("$foundSell sell offers found.", ChatType.Console)
            val price = buying.lowerKey(Int.MAX_VALUE)
            val highest = buying[price]!!
            for (offer in highest) {
                val user = accountDefinitions.getByAccount(offer.account)?.displayName
                player.message("[$id] - price: $price amount: ${offer.remaining} player: $user", ChatType.Console)
            }
        }
        player.message("${foundBuy + foundSell} results found for '$id'", ChatType.Console)
    }

    fun openItemSearch(player: Player) {
        player.open("grand_exchange_item_dialog")
        player.sendScript("item_dialogue_reset", "Grand Exchange Item Search")
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
}
