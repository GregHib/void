package content.bot.allocation

import FakeRandom
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class BuildTest {

    @BeforeEach
    fun setup() {
        setRandom(Random(1234))
    }

    @Test
    fun `Rolls stay within range`() {
        val build = Build("zerker", mapOf(Skill.Attack to 60..75, Skill.Strength to 80..99, Skill.Defence to 45..45))

        repeat(500) {
            val levels = build.roll()
            assertTrue(levels.getValue(Skill.Attack) in 60..75)
            assertTrue(levels.getValue(Skill.Strength) in 80..99)
            assertEquals(45, levels.getValue(Skill.Defence))
        }
    }

    @Test
    fun `Rolls cover the whole range`() {
        val build = Build("b", mapOf(Skill.Attack to 1..3))
        val seen = (1..300).map { build.roll().getValue(Skill.Attack) }.toSet()
        assertEquals(setOf(1, 2, 3), seen)
    }

    @Test
    fun `Fixed range gives that level`() {
        val build = Build("pure", mapOf(Skill.Defence to 60..60))
        repeat(20) {
            assertEquals(60, build.roll().getValue(Skill.Defence))
        }
    }

    @Test
    fun `Inverted range gives the first level`() {
        val build = Build("odd", mapOf(Skill.Defence to 70..60))
        assertEquals(70, build.roll().getValue(Skill.Defence))
    }

    @Test
    fun `Roll only includes the build skills`() {
        val build = Build("b", mapOf(Skill.Attack to 1..5))
        assertEquals(setOf(Skill.Attack), build.roll().keys)
    }

    @Test
    fun `Empty build rolls nothing`() {
        assertTrue(Build("empty", emptyMap()).roll().isEmpty())
    }

    @Test
    fun `Weighted random of an empty list is null`() {
        assertNull(emptyList<Pair<String, Int>>().weightedRandom())
    }

    @Test
    fun `Weighted random of a single entry ignores weight`() {
        assertEquals("a", listOf("a" to 0).weightedRandom())
    }

    private fun roll(value: Int, vararg entries: Pair<String, Int>): String? {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = value.coerceAtMost(until - 1)
        })
        return entries.toList().weightedRandom()
    }

    @Test
    fun `Weighted random picks by cumulative weight`() {
        val entries = arrayOf("a" to 1, "b" to 3, "c" to 2)

        assertEquals("a", roll(0, *entries))
        assertEquals("b", roll(1, *entries))
        assertEquals("b", roll(3, *entries))
        assertEquals("c", roll(4, *entries))
        assertEquals("c", roll(5, *entries))
    }

    @Test
    fun `Weighted random never picks zero weights`() {
        val entries = arrayOf("a" to 0, "b" to 2, "c" to -4, "d" to 1)

        for (value in 0..2) {
            assertNotEquals("a", roll(value, *entries))
            assertNotEquals("c", roll(value, *entries))
        }
        assertEquals("b", roll(0, *entries))
        assertEquals("d", roll(2, *entries))
    }

    @Test
    fun `Weighted random with all zero weights picks the first`() {
        assertEquals("a", roll(0, "a" to 0, "b" to 0))
        assertEquals("a", roll(0, "a" to -1, "b" to -1))
    }

    @Test
    fun `Role picks a build by weight`() {
        val a = Build("a", mapOf(Skill.Attack to 1..1))
        val b = Build("b", mapOf(Skill.Attack to 2..2))
        val role = BotRole("r", "activity", builds = listOf(a to 1, b to 1))
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 1
        })

        assertSame(b, role.pickBuild())
        assertNull(BotRole("none", "activity").pickBuild())
    }
}
