package content.social.trade.exchange

import com.github.michaelbull.logging.InlineLogger
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.dialogue.continueItemDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import kotlin.math.ceil
import world.gregs.voidps.engine.event.Script
@Script
class GrandExchangeOffers {

    val exchange: GrandExchange by inject()
    val itemDefinitions: ItemDefinitions by inject()
    val logger = InlineLogger()
    
    init {
        playerSpawn { player ->
            exchange.login(player)
        }

        interfaceOpen("grand_exchange") { player ->
            player.sendVariable("grand_exchange_ranges")
            player["grand_exchange_page"] = "offers"
            player["grand_exchange_box"] = -1
            player.interfaceOptions.unlockAll(id, "collect_slot_0")
            player.interfaceOptions.unlockAll(id, "collect_slot_1")
            for (i in 0 until 6) {
                exchange.refresh(player, i)
            }
        }

        interfaceClose("grand_exchange") {
            GrandExchange.clearSelection(it)
        }

        interfaceOption("Make Offer", "view_offer_*", "grand_exchange") {
            val slot = component.removePrefix("view_offer_").toInt()
            if (slot > 1 && !World.members) {
                return@interfaceOption
            }
            val offer = player.offers.getOrNull(slot) ?: return@interfaceOption
            player["grand_exchange_box"] = slot
            selectItem(player, offer.item)
        }

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

        interfaceOpen("stock_side") { player ->
            player.tab(Tab.Inventory)
            player.interfaceOptions.send(id, "items")
            player.interfaceOptions.unlockAll(id, "items", 0 until 28)
            player.sendInventory(player.inventory)
            player.sendScript("grand_exchange_hide_all")
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

    }

    /*
        Offers
     */
    
    /*
        Buy Offer
     */
    
    fun openItemSearch(player: Player) {
        player.open("grand_exchange_item_dialog")
        player.sendScript("item_dialogue_reset", "Grand Exchange Item Search")
    }
    
    /*
        Sell Offer
     */
    
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
