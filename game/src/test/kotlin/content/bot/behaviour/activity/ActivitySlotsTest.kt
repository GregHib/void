package content.bot.behaviour.activity

import content.bot.behaviour.condition.BotInArea
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.area.Rectangle

class ActivitySlotsTest {

    lateinit var slots: ActivitySlots

    @BeforeEach
    fun setup() {
        slots = ActivitySlots()
        Areas.set(
            mapOf(
                "limited" to AreaDefinition("limited", Rectangle(0, 0, 1, 1), params = mapOf(Params.BOT_CAPACITY to 1)),
                "unlimited" to AreaDefinition("unlimited", Rectangle(0, 0, 1, 1)),
            ),
        )
    }

    private fun activity(area: String) = BotActivity("test", setup = listOf(BotInArea(area))).bind(area)

    @Test
    fun `Unoccupied slots are free`() {
        assertTrue(slots.hasFree(activity("limited")))
    }

    @Test
    fun `Occupied slots aren't free`() {
        val activity = activity("limited")

        slots.occupy(activity)

        assertFalse(slots.hasFree(activity))
    }

    @Test
    fun `Released slots are free`() {
        val activity = activity("limited")

        slots.occupy(activity)
        slots.release(activity)

        assertTrue(slots.hasFree(activity))
    }

    @Test
    fun `Areas without capacity are unlimited`() {
        val activity = activity("unlimited")

        repeat(100) { slots.occupy(activity) }

        assertTrue(slots.hasFree(activity))
    }

    @Test
    fun `Activities without an area are unlimited`() {
        val activity = BotActivity("test")

        slots.occupy(activity)

        assertTrue(slots.hasFree(activity))
    }

    @Test
    fun `Unbound activity is free if any area is`() {
        val activity = BotActivity("test", setup = listOf(BotInArea(listOf("limited", "unlimited"))))
        slots.occupy(activity.bind("limited"))

        assertTrue(slots.hasFree(activity))
    }

    @Test
    fun `Reserved slots aren't free`() {
        slots.reserve("companion", "limited")

        assertFalse(slots.hasFree(activity("limited")))
        assertEquals(1, slots.occupied("limited"))
        assertEquals(0, slots.worldOccupied("limited"))
    }

    @Test
    fun `Reserving again moves the slot instead of taking another`() {
        slots.reserve("companion", "limited")
        slots.reserve("companion", "limited")
        slots.reserve("companion", "unlimited")

        assertEquals(0, slots.reserved("limited"))
        assertEquals(1, slots.reserved("unlimited"))
    }

    @Test
    fun `A companion's slot is found by its account`() {
        slots.reserve("companion", "limited")
        slots.reserve("other", "limited")
        slots.unreserve("other")

        assertEquals("limited", slots.reservedBy("companion"))
        assertNull(slots.reservedBy("other"))
        assertEquals(1, slots.reserved("limited"))
    }

    @Test
    fun `Unreserved slots are free`() {
        slots.reserve("companion", "limited")
        slots.unreserve("companion")
        slots.unreserve("companion")
        slots.unreserve("nobody")

        assertTrue(slots.hasFree(activity("limited")))
    }

    @Test
    fun `World bots and companions share one pool`() {
        Areas.set(mapOf("pair" to AreaDefinition("pair", Rectangle(0, 0, 1, 1), params = mapOf(Params.BOT_CAPACITY to 2))))
        val activity = activity("pair")
        slots.occupy(activity)
        slots.reserve("companion", "pair")

        assertFalse(slots.hasFree(activity))
    }
}
