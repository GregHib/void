package content.bot.allocation

import content.bot.Bot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player

class WeightedAllocatorTest {

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    private fun role(id: String, weight: Int = 1, min: Int = 0, max: Int? = null) = BotRole(id, "activity_$id", weight, min, max)

    private fun allocator(vararg roles: BotRole, count: Int = 0) = WeightedAllocator(AllocationConfig("test", count = count), roles.toList())

    private fun member(role: String?) = Bot(Player()).also { it.role = role }

    private fun members(vararg roles: String?) = roles.map { member(it) }

    @Test
    fun `Targets split by weight at several sizes`() {
        val allocator = allocator(role("a", 4), role("b", 3), role("c", 2), role("d", 1))

        assertArrayEquals(intArrayOf(1, 0, 0, 0), allocator.targets(1))
        assertArrayEquals(intArrayOf(1, 1, 1, 0), allocator.targets(3))
        assertArrayEquals(intArrayOf(4, 3, 2, 1), allocator.targets(10))
        assertArrayEquals(intArrayOf(40, 30, 20, 10), allocator.targets(100))
    }

    @Test
    fun `Targets always sum to the size`() {
        val allocator = allocator(role("a", 4), role("b", 3), role("c", 2), role("d", 1))
        for (size in 0..150) {
            assertEquals(size, allocator.targets(size).sum(), "size $size")
        }
    }

    @Test
    fun `Targets of size zero are all zero`() {
        val allocator = allocator(role("a", min = 2), role("b"))
        assertArrayEquals(intArrayOf(0, 0), allocator.targets(0))
    }

    @Test
    fun `Largest remainder gives leftovers to the biggest fractions`() {
        val allocator = allocator(role("a"), role("b"), role("c"))
        // 3.33 each, the leftover goes to the first of equal remainders
        assertArrayEquals(intArrayOf(4, 3, 3), allocator.targets(10))
        assertArrayEquals(intArrayOf(1, 1, 0), allocator.targets(2))
    }

    @Test
    fun `Mins are filled first in role order`() {
        val allocator = allocator(role("a", min = 2), role("b", min = 1), role("c", weight = 2))

        assertArrayEquals(intArrayOf(1, 0, 0), allocator.targets(1))
        assertArrayEquals(intArrayOf(2, 0, 0), allocator.targets(2))
        assertArrayEquals(intArrayOf(2, 1, 0), allocator.targets(3))
        assertArrayEquals(intArrayOf(3, 2, 2), allocator.targets(7))
    }

    @Test
    fun `Size below the sum of mins fills mins in role order`() {
        val allocator = allocator(role("a", min = 3), role("b", min = 3), role("c", min = 3))
        assertArrayEquals(intArrayOf(3, 2, 0), allocator.targets(5))
    }

    @Test
    fun `Min is capped by max`() {
        val allocator = allocator(role("a", min = 5, max = 2), role("b"))
        assertArrayEquals(intArrayOf(2, 8), allocator.targets(10))
    }

    @Test
    fun `Max clamps and overflow goes to other roles`() {
        val allocator = allocator(role("a", max = 1), role("b"))

        assertArrayEquals(intArrayOf(1, 9), allocator.targets(10))
        assertArrayEquals(intArrayOf(1, 0), allocator.targets(1))
    }

    @Test
    fun `Overflow is shared between remaining roles by weight`() {
        val allocator = allocator(role("a", weight = 4, max = 2), role("b", weight = 2), role("c", weight = 1))
        val targets = allocator.targets(12)

        assertEquals(2, targets[0])
        assertEquals(12, targets.sum())
        assertTrue(targets[1] > targets[2])
    }

    @Test
    fun `Sum is less than size when every role hits its max`() {
        val allocator = allocator(role("a", max = 2), role("b", max = 3))
        assertArrayEquals(intArrayOf(2, 3), allocator.targets(10))
    }

    @Test
    fun `Zero weight roles only get their min`() {
        val allocator = allocator(role("a", weight = 0, min = 2), role("b"))

        assertArrayEquals(intArrayOf(2, 8), allocator.targets(10))
        assertArrayEquals(intArrayOf(1, 0), allocator.targets(1))
    }

    @Test
    fun `All zero weight roles stop at their mins`() {
        val allocator = allocator(role("a", weight = 0, min = 1), role("b", weight = 0))
        assertArrayEquals(intArrayOf(1, 0), allocator.targets(10))
    }

    @Test
    fun `Allocate picks the role with the largest shortfall`() {
        val allocator = allocator(role("a", 3), role("b", 1), count = 4)

        assertEquals("a", allocator.allocate(emptyList())!!.id)
        assertEquals("a", allocator.allocate(members("a", "b"))!!.id)
        assertEquals("b", allocator.allocate(members("a", "a", "a"))!!.id)
    }

    @Test
    fun `Allocate fills the emptiest role`() {
        val allocator = allocator(role("a", 1), role("b", 1), role("c", 1), count = 6)

        assertEquals("c", allocator.allocate(members("a", "a", "b"))!!.id)
        assertEquals("b", allocator.allocate(members("a", "a", "c"))!!.id)
    }

    @Test
    fun `Allocate ties go to the higher weight`() {
        val allocator = allocator(role("a", 1), role("b", 2), count = 3)
        // Targets [1, 2], current [0, 1] - both have a shortfall of one
        assertEquals("b", allocator.allocate(members("b"))!!.id)
    }

    @Test
    fun `Allocate ties between equal weights go to the first role`() {
        val allocator = allocator(role("a"), role("b"), count = 2)
        assertEquals("a", allocator.allocate(emptyList())!!.id)
    }

    @Test
    fun `Allocate ignores members with no or unknown role`() {
        val allocator = allocator(role("a", 3), role("b", 1), count = 4)
        assertEquals("a", allocator.allocate(members(null, "unknown"))!!.id)
    }

    @Test
    fun `Members exceeding the count are still assigned proportionally`() {
        val allocator = allocator(role("a", 3), role("b", 1), count = 2)
        // 4 members + 1 = 5 -> targets [4, 1] (3.75, 1.25), current [4, 0]
        assertEquals("b", allocator.allocate(members("a", "a", "a", "a"))!!.id)
        // 8 members + 1 = 9 -> targets [7, 2] (6.75, 2.25), current [6, 2]
        assertEquals("a", allocator.allocate(members("a", "a", "a", "a", "a", "a", "b", "b"))!!.id)
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
        val allocator = CapacityAllocator(AllocationConfig("capacity", count = 10))

        assertNull(allocator.allocate(emptyList()))
        assertNull(allocator.allocate(members("a")))
        assertNull(allocator.role("a"))
    }

    @Test
    fun `Count uses the config value without a setting`() {
        val allocator = CapacityAllocator(AllocationConfig("test", count = 7, spawnSeconds = 30))

        assertEquals(7, allocator.count)
        assertEquals(30, allocator.spawnSeconds)
        assertEquals("test", allocator.id)
    }

    @Test
    fun `Count and spawn seconds are overridden by settings`() {
        Settings.load(mapOf("bots.count" to "25", "bots.spawnSeconds" to "5"))
        val allocator = CapacityAllocator(AllocationConfig("test", count = 7, countSetting = "bots.count", spawnSeconds = 30, spawnSecondsSetting = "bots.spawnSeconds"))

        assertEquals(25, allocator.count)
        assertEquals(5, allocator.spawnSeconds)
    }

    @Test
    fun `Count and spawn seconds fall back to config when the setting is missing`() {
        Settings.load(mapOf("other" to "1"))
        val allocator = CapacityAllocator(AllocationConfig("test", count = 7, countSetting = "bots.count", spawnSeconds = 30, spawnSecondsSetting = "bots.spawnSeconds"))

        assertEquals(7, allocator.count)
        assertEquals(30, allocator.spawnSeconds)
    }

    @Test
    fun `Setting count changes allocation targets`() {
        Settings.load(mapOf("bots.count" to "4"))
        val allocator = WeightedAllocator(AllocationConfig("test", count = 0, countSetting = "bots.count"), listOf(role("a", 3), role("b", 1)))

        assertEquals("a", allocator.allocate(emptyList())!!.id)
        assertEquals("b", allocator.allocate(members("a", "a", "a"))!!.id)
    }
}
