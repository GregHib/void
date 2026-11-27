package content.bot.allocation

import content.bot.Bot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player

class BotWeightedSpawnTest {

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    private fun role(id: String, weight: Int = 1, min: Int = 0, max: Int? = null) = BotRole(id, "activity_$id", weight, min, max)

    private fun allocator(vararg roles: BotRole, count: Int = 0) = BotWeightedSpawn(BotSpawnConfig("test", count = count), roles.toList())

    private fun member(role: String?) = Bot(Player()).also { it.role = role }

    private fun members(vararg roles: String?) = roles.map { member(it) }

    /**
     * Spawn [size] bots one after another, returning the role id each one was given ("-" for none).
     */
    private fun BotWeightedSpawn.sequence(size: Int): List<String> {
        val members = mutableListOf<Bot>()
        repeat(size) { members.add(member(allocate(members)?.id)) }
        return members.map { it.role ?: "-" }
    }

    private fun BotWeightedSpawn.counts(size: Int): List<Int> {
        val sequence = sequence(size)
        return roles.map { role -> sequence.count { it == role.id } }
    }

    @Test
    fun `Spawns are split by weight`() {
        val allocator = allocator(role("a", 4), role("b", 3), role("c", 2), role("d", 1))

        assertEquals("a b c d a b a c b a".split(" "), allocator.sequence(10))
        assertEquals(listOf(40, 30, 20, 10), allocator.counts(100))
    }

    @Test
    fun `Equal weights take turns in role order`() {
        val allocator = allocator(role("a"), role("b"), role("c"))
        assertEquals("a b c a b c a".split(" "), allocator.sequence(7))
    }

    @Test
    fun `Mins are filled first in role order`() {
        val allocator = allocator(role("a", min = 2), role("b", min = 1), role("c", weight = 2))
        assertEquals("a a b c c c b".split(" "), allocator.sequence(7))
    }

    @Test
    fun `Min is capped by max`() {
        val allocator = allocator(role("a", min = 5, max = 2), role("b"))
        assertEquals("a a b b b".split(" "), allocator.sequence(5))
    }

    @Test
    fun `Max clamps and overflow goes to other roles`() {
        val allocator = allocator(role("a", max = 1), role("b"))
        assertEquals("a b b b".split(" "), allocator.sequence(4))
    }

    @Test
    fun `Overflow is shared between remaining roles by weight`() {
        val allocator = allocator(role("a", weight = 4, max = 2), role("b", weight = 2), role("c", weight = 1))
        val counts = allocator.counts(12)

        assertEquals(2, counts[0])
        assertEquals(12, counts.sum())
        assertTrue(counts[1] > counts[2])
    }

    @Test
    fun `No role once every role hits its max`() {
        val allocator = allocator(role("a", max = 2), role("b", max = 3))
        assertEquals("a b a b b - -".split(" "), allocator.sequence(7))
    }

    @Test
    fun `Zero weight roles only get their min`() {
        val allocator = allocator(role("a", weight = 0, min = 2), role("b"))
        assertEquals("a a b b b".split(" "), allocator.sequence(5))
    }

    @Test
    fun `All zero weight roles stop at their mins`() {
        val allocator = allocator(role("a", weight = 0, min = 1), role("b", weight = 0))
        assertEquals("a - -".split(" "), allocator.sequence(3))
    }

    @Test
    fun `Allocate picks the role with the fewest bots per weight`() {
        val allocator = allocator(role("a", 3), role("b", 1))

        assertEquals("a", allocator.allocate(emptyList())!!.id)
        assertEquals("a", allocator.allocate(members("a", "b"))!!.id)
        assertEquals("b", allocator.allocate(members("a", "a", "a"))!!.id)
    }

    @Test
    fun `Allocate refills the role that lost bots`() {
        val allocator = allocator(role("a", 1), role("b", 1), role("c", 1))

        assertEquals("c", allocator.allocate(members("a", "a", "b", "b", "c"))!!.id)
        assertEquals("b", allocator.allocate(members("a", "a", "c", "c"))!!.id)
    }

    @Test
    fun `Allocate ties go to the higher weight`() {
        val allocator = allocator(role("a", 1), role("b", 2))
        assertEquals("b", allocator.allocate(emptyList())!!.id)
        // a: 1/1, b: 2/2
        assertEquals("b", allocator.allocate(members("a", "b", "b"))!!.id)
    }

    @Test
    fun `Allocate ties between equal weights go to the first role`() {
        val allocator = allocator(role("a"), role("b"))
        assertEquals("a", allocator.allocate(emptyList())!!.id)
    }

    @Test
    fun `Allocate ignores members with no or unknown role`() {
        val allocator = allocator(role("a", 3), role("b", 1))
        assertEquals("a", allocator.allocate(members(null, "unknown"))!!.id)
    }

    @Test
    fun `Allocate with no roles is null`() {
        assertNull(allocator(count = 5).allocate(emptyList()))
    }

    @Test
    fun `Role lookup by id`() {
        val a = role("a")
        val allocator = allocator(a, role("b"))

        assertSame(a, allocator.role("a"))
        assertNull(allocator.role("missing"))
    }

    @Test
    fun `Capacity allocator allocates no role`() {
        val allocator = BotCapacitySpawn(BotSpawnConfig("capacity", count = 10))

        assertNull(allocator.allocate(emptyList()))
        assertNull(allocator.allocate(members("a")))
        assertNull(allocator.role("a"))
    }

    @Test
    fun `Count uses the config value without a setting`() {
        val allocator = BotCapacitySpawn(BotSpawnConfig("test", count = 7, spawnSeconds = 30))

        assertEquals(7, allocator.count)
        assertEquals(30, allocator.spawnSeconds)
        assertEquals("test", allocator.id)
    }

    @Test
    fun `Count and spawn seconds are overridden by settings`() {
        Settings.load(mapOf("bots.count" to "25", "bots.spawnSeconds" to "5"))
        val allocator = BotCapacitySpawn(BotSpawnConfig("test", count = 7, countSetting = "bots.count", spawnSeconds = 30, spawnSecondsSetting = "bots.spawnSeconds"))

        assertEquals(25, allocator.count)
        assertEquals(5, allocator.spawnSeconds)
    }

    @Test
    fun `Count and spawn seconds fall back to config when the setting is missing`() {
        Settings.load(mapOf("other" to "1"))
        val allocator = BotCapacitySpawn(BotSpawnConfig("test", count = 7, countSetting = "bots.count", spawnSeconds = 30, spawnSecondsSetting = "bots.spawnSeconds"))

        assertEquals(7, allocator.count)
        assertEquals(30, allocator.spawnSeconds)
    }
}
