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
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GrandExchangeTest : WorldTest() {

    private lateinit var exchange: GrandExchange
    private lateinit var clerk: NPC

    @BeforeEach
    fun setup() {
        exchange = get()
        exchange.offers.clear()
        clerk = createNPC("grand_exchange_clerk_short", Tile(3164, 3488))
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
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 1, coins = 19000), seller, 1)
        assertOffer(expectedBuy.copy(state = OfferState.CompletedBuy, completed = 1), buyer, 0)


        collect(buyer, 0)
        collect(seller, 1)

        assertEquals(1, buyer.inventory.count("rune_longsword_noted"))
        assertEquals(1000, buyer.inventory.count("coins"))
        assertEquals(0, seller.inventory.count("rune_longsword"))
        assertEquals(19000, seller.inventory.count("coins"))
    }

    @Test
    fun `Sell item priced more than existing offer does nothing`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("abyssal_whip")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 1_000_000)

        buy(buyer, "abyssal_whip")
        buyer.interfaceOption("grand_exchange", "add_1", "Add 1")
        confirm(buyer)
        val expectedBuy = Offer(1, "abyssal_whip", 1, 865_326, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)


        sell(seller, "abyssal_whip")
        seller.interfaceOption("grand_exchange", "offer_max", "Offer Maximum Price")
        confirm(seller)
        val expectedSell = Offer(2, "abyssal_whip", 1, 908_593, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        tick()

        assertOffer(expectedBuy.copy(state = OfferState.OpenBuy), buyer, 0)
        assertOffer(expectedSell.copy(state = OfferState.OpenSell), seller, 1)
    }

    @Test
    fun `Sell item underpriced sells to highest buyer`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("flax_noted", 100)
        val buyer1 = createPlayer(Tile(3164, 3487), "buyer")
        buyer1.inventory.add("coins", 10_000)
        val buyer2 = createPlayer(Tile(3164, 3487), "buyer2")
        buyer2.inventory.add("coins", 10_000)

        buy(buyer1, "flax")
        buyer1.interfaceOption("grand_exchange", "add_100", "Add 100")
        confirm(buyer1)
        val expectedBuy1 = Offer(1, "flax", 100, 67, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy1, buyer1, 0)

        buy(buyer2, "flax")
        buyer2.interfaceOption("grand_exchange", "add_100", "Add 100")
        buyer2.interfaceOption("grand_exchange", "offer_max", "Offer Maximum Price")
        confirm(buyer2)
        val expectedBuy2 = Offer(2, "flax", 100, 71, OfferState.PendingBuy, account = "buyer2")
        assertOffer(expectedBuy2, buyer2, 0)


        sell(seller, "flax_noted")
        seller.interfaceOption("grand_exchange", "offer_min", "Offer Minimum Price")
        confirm(seller)
        val expectedSell = Offer(3, "flax", 100, 64, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        tick()

        assertOffer(expectedBuy1.copy(state = OfferState.OpenBuy), buyer1, 0)
        assertOffer(expectedBuy2.copy(state = OfferState.CompletedBuy, completed = 100), buyer2, 0)
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 100, coins = 7100), seller, 1)
        val buyerBox = buyer2.inventories.inventory("collection_box_0")
        val sellerBox = seller.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("flax", 100))
        assertTrue(sellerBox.contains("coins", 7100))
    }

    @Test
    fun `Buying over price will give lowest open sell offer`() {
        val seller1 = createPlayer(Tile(3164, 3487), "seller")
        seller1.inventory.add("abyssal_whip")
        val seller2 = createPlayer(Tile(3164, 3487), "seller")
        seller2.inventory.add("abyssal_whip")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 1_000_000)

        sell(seller1, "abyssal_whip")
        confirm(seller1)
        val expectedSell = Offer(1, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller1, 1)

        sell(seller2, "abyssal_whip")
        seller2.interfaceOption("grand_exchange", "offer_max", "Offer Maximum Price")
        confirm(seller2)
        assertOffer(expectedSell.copy(id = 2, price = 908_593), seller2, 1)

        buy(buyer, "abyssal_whip")
        buyer.interfaceOption("grand_exchange", "add_1", "Add 1")
        buyer.interfaceOption("grand_exchange", "offer_max", "Offer Maximum Price")
        confirm(buyer)
        val expectedBuy = Offer(3, "abyssal_whip", 1, 908_593, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)

        tick()

        assertOffer(expectedSell.copy(id = 2, price = 908_593, state = OfferState.OpenSell), seller2, 1)
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 1, coins = 865326), seller1, 1)
        assertOffer(expectedBuy.copy(state = OfferState.CompletedBuy, completed = 1, coins = 43267), buyer, 0)
        val buyerBox = buyer.inventories.inventory("collection_box_0")
        val sellerBox = seller1.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("abyssal_whip"))
        assertTrue(buyerBox.contains("coins", 43_267))
        assertTrue(sellerBox.contains("coins", 865_326))
    }

    @Test
    fun `Buy item priced less than existing offer does nothing`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("abyssal_whip")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 1_000_000)


        sell(seller, "abyssal_whip")
        confirm(seller)
        val expectedSell = Offer(1, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        buy(buyer, "abyssal_whip")
        buyer.interfaceOption("grand_exchange", "add_1", "Add 1")
        buyer.interfaceOption("grand_exchange", "offer_min", "Offer Minimum Price")
        confirm(buyer)
        val expectedBuy = Offer(2, "abyssal_whip", 1, 822_060, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)

        tick()

        assertOffer(expectedBuy.copy(state = OfferState.OpenBuy), buyer, 0)
        assertOffer(expectedSell.copy(state = OfferState.OpenSell), seller, 1)
    }

    @Test
    fun `Multiple buy offers at same price samples one`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("fire_rune", 100)
        val buyer1 = createPlayer(Tile(3164, 3487), "buyer")
        buyer1.inventory.add("coins", 10_000)
        val buyer2 = createPlayer(Tile(3164, 3487), "buyer2")
        buyer2.inventory.add("coins", 10_000)

        buy(buyer1, "fire_rune")
        buyer1.interfaceOption("grand_exchange", "add_100", "Add 100")
        confirm(buyer1)
        val expectedBuy1 = Offer(1, "fire_rune", 100, 4, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy1, buyer1, 0)

        buy(buyer2, "fire_rune")
        buyer2.interfaceOption("grand_exchange", "add_100", "Add 100")
        confirm(buyer2)
        val expectedBuy2 = Offer(2, "fire_rune", 100, 4, OfferState.PendingBuy, account = "buyer2")
        assertOffer(expectedBuy2, buyer2, 0)

        sell(seller, "fire_rune")
        confirm(seller)
        val expectedSell = Offer(3, "fire_rune", 100, 4, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        tick()

        assertOffer(expectedBuy1.copy(state = OfferState.CompletedBuy, completed = 100), buyer1, 0)
        assertOffer(expectedBuy2.copy(state = OfferState.OpenBuy), buyer2, 0)
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 100, coins = 400), seller, 1)
        val buyerBox = buyer1.inventories.inventory("collection_box_0")
        val sellerBox = seller.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("fire_rune", 100))
        assertTrue(sellerBox.contains("coins", 50))
    }

    @Test
    fun `Multiple sell offers at same price samples one`() {
        val seller1 = createPlayer(Tile(3164, 3487), "seller1")
        seller1.inventory.add("abyssal_whip")
        val seller2 = createPlayer(Tile(3164, 3487), "seller2")
        seller2.inventory.add("abyssal_whip")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 1_000_000)

        sell(seller1, "abyssal_whip")
        confirm(seller1)
        val expectedSell1 = Offer(1, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller1")
        assertOffer(expectedSell1, seller1, 1)

        sell(seller2, "abyssal_whip")
        confirm(seller2)
        val expectedSell2 = Offer(2, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller2")
        assertOffer(expectedSell2, seller2, 1)

        buy(buyer, "abyssal_whip")
        buyer.interfaceOption("grand_exchange", "add_1", "Add 1")
        confirm(buyer)
        val expectedBuy = Offer(3, "abyssal_whip", 1, 865_326, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)

        tick()

        assertOffer(expectedSell1.copy(state = OfferState.CompletedSell, completed = 1, coins = 865_326), seller1, 1)
        assertOffer(expectedSell2.copy(state = OfferState.OpenSell), seller2, 1)
        assertOffer(expectedBuy.copy(state = OfferState.CompletedBuy, completed = 1), buyer, 0)
        val buyerBox = buyer.inventories.inventory("collection_box_0")
        val sellerBox = seller1.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("abyssal_whip"))
        assertEquals(0, buyerBox.count("coins"))
        assertTrue(sellerBox.contains("coins", 865_326))
    }

    @Test
    fun `Buy more amount than available`() {
        val seller1 = createPlayer(Tile(3164, 3487), "seller1")
        seller1.inventory.add("abyssal_whip")
        val seller2 = createPlayer(Tile(3164, 3487), "seller2")
        seller2.inventory.add("abyssal_whip")
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 10_000_000)

        sell(seller1, "abyssal_whip")
        confirm(seller1)
        val expectedSell1 = Offer(1, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller1")
        assertOffer(expectedSell1, seller1, 1)

        sell(seller2, "abyssal_whip")
        confirm(seller2)
        val expectedSell2 = Offer(2, "abyssal_whip", 1, 865_326, OfferState.PendingSell, account = "seller2")
        assertOffer(expectedSell2, seller2, 1)

        buy(buyer, "abyssal_whip")
        buyer.interfaceOption("grand_exchange", "add_10", "Add 10")
        buyer.interfaceOption("grand_exchange", "offer_max", "Offer Maximum Price")
        confirm(buyer)
        val expectedBuy = Offer(3, "abyssal_whip", 10, 908_593, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)

        tick()

        assertOffer(expectedSell1.copy(state = OfferState.CompletedSell, completed = 1, coins = 865326), seller1, 1)
        assertOffer(expectedSell2.copy(state = OfferState.CompletedSell, completed = 1, coins = 865326), seller2, 1)
        assertOffer(expectedBuy.copy(state = OfferState.OpenBuy, completed = 2, coins = 86_534), buyer, 0)
        val buyerBox = buyer.inventories.inventory("collection_box_0")
        val sellerBox = seller1.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("abyssal_whip"))
        assertTrue(buyerBox.contains("coins", 86_534))
        assertTrue(sellerBox.contains("coins", 865_326))
    }

    @Test
    fun `Buy less amount than available`() {
        val seller1 = createPlayer(Tile(3164, 3487), "seller1")
        seller1.inventory.add("spirit_shards", 1_000)
        val seller2 = createPlayer(Tile(3164, 3487), "seller2")
        seller2.inventory.add("spirit_shards", 1_000)
        val buyer = createPlayer(Tile(3164, 3487), "buyer")
        buyer.inventory.add("coins", 1_000_000)

        sell(seller1, "spirit_shards")
        confirm(seller1)
        val expectedSell1 = Offer(1, "spirit_shards", 1_000, 24, OfferState.PendingSell, account = "seller1")
        assertOffer(expectedSell1, seller1, 1)

        sell(seller2, "spirit_shards")
        confirm(seller2)
        val expectedSell2 = Offer(2, "spirit_shards", 1_000, 24, OfferState.PendingSell, account = "seller2")
        assertOffer(expectedSell2, seller2, 1)

        buy(buyer, "spirit_shards")
        buyer.interfaceOption("grand_exchange", "add_x", "Edit Quantity")
        (buyer.dialogueSuspension as? IntSuspension)?.resume(1_500)
        confirm(buyer)
        val expectedBuy = Offer(3, "spirit_shards", 1_500, 24, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy, buyer, 0)

        tick()

        assertOffer(expectedSell1.copy(state = OfferState.CompletedSell, completed = 1_000, coins = 24_000), seller1, 1)
        assertOffer(expectedSell2.copy(state = OfferState.OpenSell, completed = 500, coins = 12_000), seller2, 1)
        assertOffer(expectedBuy.copy(state = OfferState.CompletedBuy, completed = 1_500), buyer, 0)
        val buyerBox = buyer.inventories.inventory("collection_box_0")
        val sellerBox = seller1.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("spirit_shards", 1_500))
        assertTrue(sellerBox.contains("coins", 24_000))
    }

    @Test
    fun `Sell more amount than demand`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("fire_rune", 100)
        val buyer1 = createPlayer(Tile(3164, 3487), "buyer")
        buyer1.inventory.add("coins", 1_000)
        val buyer2 = createPlayer(Tile(3164, 3487), "buyer2")
        buyer2.inventory.add("coins", 1_000)

        buy(buyer1, "fire_rune")
        buyer1.interfaceOption("grand_exchange", "add_10", "Add 10")
        confirm(buyer1)
        val expectedBuy1 = Offer(1, "fire_rune", 10, 4, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy1, buyer1, 0)

        buy(buyer2, "fire_rune")
        buyer2.interfaceOption("grand_exchange", "add_10", "Add 10")
        confirm(buyer2)
        val expectedBuy2 = Offer(2, "fire_rune", 10, 4, OfferState.PendingBuy, account = "buyer2")
        assertOffer(expectedBuy2, buyer2, 0)

        sell(seller, "fire_rune")
        confirm(seller)
        val expectedSell = Offer(3, "fire_rune", 100, 4, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        tick()

        assertOffer(expectedBuy1.copy(state = OfferState.CompletedBuy, completed = 10), buyer1, 0)
        assertOffer(expectedBuy2.copy(state = OfferState.CompletedBuy, completed = 10), buyer2, 0)
        assertOffer(expectedSell.copy(state = OfferState.OpenSell, completed = 20, coins = 80), seller, 1)
        val buyerBox = buyer1.inventories.inventory("collection_box_0")
        val sellerBox = seller.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("fire_rune", 10))
        assertTrue(sellerBox.contains("coins", 80))
    }

    @Test
    fun `Sell less amount than demand`() {
        val seller = createPlayer(Tile(3164, 3487), "seller")
        seller.inventory.add("fire_rune", 15)
        val buyer1 = createPlayer(Tile(3164, 3487), "buyer")
        buyer1.inventory.add("coins", 1_000)
        val buyer2 = createPlayer(Tile(3164, 3487), "buyer2")
        buyer2.inventory.add("coins", 1_000)

        buy(buyer1, "fire_rune")
        buyer1.interfaceOption("grand_exchange", "add_10", "Add 10")
        confirm(buyer1)
        val expectedBuy1 = Offer(1, "fire_rune", 10, 4, OfferState.PendingBuy, account = "buyer")
        assertOffer(expectedBuy1, buyer1, 0)

        buy(buyer2, "fire_rune")
        buyer2.interfaceOption("grand_exchange", "add_10", "Add 10")
        confirm(buyer2)
        val expectedBuy2 = Offer(2, "fire_rune", 10, 4, OfferState.PendingBuy, account = "buyer2")
        assertOffer(expectedBuy2, buyer2, 0)

        sell(seller, "fire_rune")
        confirm(seller)
        val expectedSell = Offer(3, "fire_rune", 15, 4, OfferState.PendingSell, account = "seller")
        assertOffer(expectedSell, seller, 1)

        tick()

        assertOffer(expectedBuy1.copy(state = OfferState.CompletedBuy, completed = 10), buyer1, 0)
        assertOffer(expectedBuy2.copy(state = OfferState.OpenBuy, completed = 5), buyer2, 0)
        assertOffer(expectedSell.copy(state = OfferState.CompletedSell, completed = 15, coins = 60), seller, 1)
        val buyerBox = buyer1.inventories.inventory("collection_box_0")
        val sellerBox = seller.inventories.inventory("collection_box_1")
        assertTrue(buyerBox.contains("fire_rune", 10))
        assertTrue(sellerBox.contains("coins", 60))
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
}