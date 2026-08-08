package content.social.trade.monitor

import WorldTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ItemCensusTest : WorldTest() {

    private val offers = OpenOffers()

    @BeforeEach
    fun setup() {
        Settings.load(
            mapOf(
                "economy.monitor.enabled" to "true",
                "economy.monitor.census.enabled" to "true",
                "economy.monitor.census.valueThreshold" to "2000000000",
                "economy.monitor.census.items" to "coins,rune_longsword",
                "economy.monitor.census.driftPercent" to "0.01",
            ),
        )
        ItemCensus.clear()
        ItemCensus.reload()
        AuditLog.logs.clear()
    }

    @AfterEach
    fun teardown() {
        ItemCensus.clear()
    }

    @Test
    fun `Items added to player inventories are counted`() {
        val player = createPlayer(emptyTile, "census_add")
        player.inventory.add("rune_longsword")
        player.inventory.add("coins", 500)
        val counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(1L, counts["rune_longsword"]?.players)
        assertEquals(500L, counts["coins"]?.players)
    }

    @Test
    fun `Noted items count towards the base item`() {
        val player = createPlayer(emptyTile, "census_noted")
        player.inventory.add("rune_longsword_noted", 5)
        val counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(5L, counts["rune_longsword"]?.players)
    }

    @Test
    fun `Floor items are tracked in a separate bucket`() {
        val item = createFloorItem("rune_longsword", emptyTile)
        tick()
        var counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(1L, counts["rune_longsword"]?.floor)
        assertEquals(1L, counts["rune_longsword"]?.total)
        FloorItems.remove(item)
        tick()
        counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(0L, counts["rune_longsword"]?.floor)
    }

    @Test
    fun `Mirror inventories are ignored`() {
        val player = createPlayer(emptyTile, "census_mirror")
        player.inventories.inventory("trade_offer", secondary = true).add("rune_longsword")
        val counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(0L, counts["rune_longsword"]?.players)
    }

    @Test
    fun `Snapshot round trips through the census store`() {
        val player = createPlayer(emptyTile, "census_store")
        player.inventory.add("coins", 1234)
        val file = File(Settings["storage.players.path"]).resolve("economy/census.toml")
        CensusStore.save(ItemCensus.snapshot(offers), file)
        val loaded = CensusStore.load(file)
        assertEquals(1234L, loaded["coins"])
    }

    @Test
    fun `Reconciliation overwrites drifted counters and logs`() {
        val player = createPlayer(emptyTile, "census_drift")
        player.inventory.add("coins", 1000)
        ItemCensus.apply(mapOf("coins" to 500L))
        assertTrue(AuditLog.logs.any { it.contains("CENSUS_DRIFT") && it.contains("expected=1000") && it.contains("actual=500") })
        val counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(500L, counts["coins"]?.players)
    }

    @Test
    fun `Drift within tolerance is corrected silently`() {
        val player = createPlayer(emptyTile, "census_tolerance")
        player.inventory.add("coins", 1000)
        ItemCensus.apply(mapOf("coins" to 995L))
        assertTrue(AuditLog.logs.none { it.contains("CENSUS_DRIFT") })
        val counts = ItemCensus.snapshot(offers).associateBy { it.item }
        assertEquals(995L, counts["coins"]?.players)
    }
}
