package content.social.trade.exchange

import WorldTest
import content.social.trade.exchange.offer.Offer
import content.social.trade.exchange.offer.OfferState
import interfaceOption
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.client.ui.dialogue.ContinueItemDialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GrandExchangeTest : WorldTest() {


    private lateinit var exchange: GrandExchange
    private lateinit var clerk: NPC

    @BeforeEach
    fun setup() {
        exchange = get()
        clerk = createNPC("grand_exchange_clerk_short", Tile(3164, 3488))
    }

    private fun buy(player: Player, item: String) {
        player.npcOption(clerk, "Exchange")
        tick()
        player.interfaceOption("grand_exchange", "buy_offer_0", "Make Buy Offer")
        tick()
        player.emit(ContinueItemDialogue(item))
    }

    private fun sell(player: Player, item: String) {
        player.npcOption(clerk, "Exchange")
        tick()
        player.interfaceOption("grand_exchange", "sell_offer_1", "Make Sell Offer")
        tick()
        player.interfaceOption("stock_side", "items", "Offer", item = Item(item, 1), slot = player.inventory.indexOf(item))
    }

    private fun confirm(player: Player) {
        player.interfaceOption("grand_exchange", "confirm", "Confirm Offer")
    }

    private fun collect(player: Player, slot: Int) {
        player.interfaceOption("grand_exchange", "view_offer_${slot}", "Make Offer")
        tick()
        player.interfaceOption("grand_exchange", "collect_slot_0", "Collect_notes")
        player.interfaceOption("grand_exchange", "collect_slot_1", "Collect_notes")
    }

    private fun assertOffer(expected: Offer, player: Player, slot: Int) {
        assertEquals(expected.id, player["grand_exchange_offer_${slot}", -1])
        val sellOffer = exchange.offers.offer(expected.id)
        assertNotNull(sellOffer)
        assertEquals(expected, sellOffer)
    }

    @Test
    fun `Sell item mid price`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("rune_longsword")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 20_000)


        sell(seller, "rune_longsword")
        confirm(seller)
        val expectedSell = Offer(1, "rune_longsword", 1, 19_000, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)
        tick()
        assertOffer(expectedSell.copy(state = OfferState.OpenSell), seller, 1)

        buy(buyer, "rune_longsword")
        buyer.interfaceOption("grand_exchange", "add_1", "Add 1")
        confirm(buyer)

        val expectedBuy = Offer(2, "rune_longsword", 1, 19_000, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)
        tick()
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 1), seller, 1)
        assertOffer(expectedBuy.copy(state = OfferState.CompletedBuy, completed = 1), buyer, 0)


        collect(buyer, 0)
        collect(seller, 1)

        assertEquals(1, buyer.inventory.count("rune_longsword_noted"))
        assertEquals(1000, buyer.inventory.count("coins"))
        assertEquals(0, seller.inventory.count("rune_longsword"))
        assertEquals(19000, seller.inventory.count("coins"))
    }
}