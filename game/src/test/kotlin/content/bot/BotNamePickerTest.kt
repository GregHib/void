package content.bot

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BotNamePickerTest {
    @Test
    fun `Configured percentage controls spawn choices when both pools are available`() {
        val random = Random(42)
        val saved = (1..10000).count {
            BotNamePicker.pick(listOf("saved"), 80, random, { true }, { "new" }) == "saved"
        }
        kotlin.test.assertTrue(saved in 7800..8200, "Expected about 80% saved spawns, got $saved of 10000")
    }

    @Test
    fun `Saved preference skips unavailable names and reserves only one`() {
        val reserved = mutableListOf<String>()
        val name = BotNamePicker.pick(listOf("online", "saved"), 100, Random(1), {
            if (it == "online") false else { reserved.add(it); true }
        }, { error("Fresh pool should not be used") })
        assertEquals("saved", name)
        assertEquals(listOf("saved"), reserved)
    }

    @Test
    fun `Fresh preference leaves saved names unreserved`() {
        assertEquals("new", BotNamePicker.pick(listOf("saved"), 0, Random(1), { error("Saved pool should not be used") }, { "new" }))
    }

    @Test
    fun `Exhausted pools fall back in either direction`() {
        assertEquals("new", BotNamePicker.pick(listOf("online"), 100, Random(1), { false }, { "new" }))
        assertEquals("saved", BotNamePicker.pick(listOf("saved"), 0, Random(1), { true }, { null }))
        assertNull(BotNamePicker.pick(emptyList(), 50, Random(1), { true }, { null }))
    }
}
