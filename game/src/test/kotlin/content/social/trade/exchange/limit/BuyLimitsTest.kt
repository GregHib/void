package content.social.trade.exchange.limit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import java.util.concurrent.TimeUnit

class BuyLimitsTest {

    private lateinit var definition: ItemDefinition
    private lateinit var buyLimits: BuyLimits

    @BeforeEach
    fun setup() {
        definition = ItemDefinition(stringId = "item")
        ItemDefinitions.set(arrayOf(definition), mapOf("item" to 0))
        buyLimits = BuyLimits()
    }

    @Test
    fun `Record accumulates limits per player`() {
        val itemId = "item"
        val player = "player"

        definition.extras = mapOf("limit" to 100)

        buyLimits.record(player, itemId, 30)
        buyLimits.record(player, itemId, 10)
        buyLimits.record("other", itemId, 10)

        val remaining = buyLimits.limit(player, itemId)
        assertEquals(60, remaining) // 100 - (30 + 10)
        assertEquals(90, buyLimits.limit("other", itemId))
    }

    @Test
    fun `Limit returns remaining when no record`() {
        definition.extras = mapOf("limit" to 50)

        val remaining = buyLimits.limit("player", "item")
        assertEquals(50, remaining)
    }

    @Test
    fun `Limit returns -1 if no limit`() {
        val remaining = buyLimits.limit("player", "unknown")
        assertEquals(-1, remaining)
    }

    @Test
    fun `Tick removes entries older than 4 hours`() {
        val oldTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)

        // Inject old data directly
        definition.extras = mapOf("limit" to 100)
        buyLimits.record("player", "item", 20, oldTimestamp)

        buyLimits.tick()

        assertEquals(100, buyLimits.limit("player", "item"))
    }

    @Test
    fun `Tick doesn't remove entries newer than 4 hours`() {
        val recentTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)

        definition.extras = mapOf("limit" to 100)
        buyLimits.record("player", "item", 10, recentTimestamp)

        buyLimits.tick()

        assertEquals(90, buyLimits.limit("player", "item"))
    }
}
