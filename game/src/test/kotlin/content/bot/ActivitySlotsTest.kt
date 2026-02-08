package content.bot

import content.bot.behaviour.activity.ActivitySlots
import content.bot.behaviour.activity.BotActivity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActivitySlotsTest {

    lateinit var slots: ActivitySlots

    @BeforeEach
    fun setup() {
        slots = ActivitySlots()
    }

    @Test
    fun `Unoccupied slots are free`() {
        val activity = BotActivity("test", 2)

        assertTrue(slots.hasFree(activity))
    }

    @Test
    fun `Occupied slots aren't free`() {
        val activity = BotActivity("test", 1)

        slots.occupy(activity)

        assertFalse(slots.hasFree(activity))
    }

    @Test
    fun `Released slots are free`() {
        val activity = BotActivity("test", 1)

        slots.occupy(activity)
        slots.release(activity)

        assertTrue(slots.hasFree(activity))
    }
}
