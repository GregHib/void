package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResourceRespawnTest {

    @Test
    fun `Near max population returns base time`() {
        assertEquals(100, ResourceRespawn.ticks(100, players = 1948))
        assertEquals(100, ResourceRespawn.ticks(100, players = 2048))
    }

    @Test
    fun `Low population uses maximum factor`() {
        // 100 + 7 * sqrt(100) / 0.6 = 216.67
        assertEquals(217, ResourceRespawn.ticks(100, players = 0))
        assertEquals(217, ResourceRespawn.ticks(100, players = 1))
        assertEquals(217, ResourceRespawn.ticks(100, players = 249))
        assertEquals(217, ResourceRespawn.ticks(100, players = 499))
    }

    @Test
    fun `Factor decreases with population`() {
        // 100 + 6 * sqrt(100) / 0.6 = 200
        assertEquals(200, ResourceRespawn.ticks(100, players = 500))
        // 100 + 1 * sqrt(100) / 0.6 = 116.67
        assertEquals(117, ResourceRespawn.ticks(100, players = 1750))
        assertEquals(117, ResourceRespawn.ticks(100, players = 1947))
    }

    @Test
    fun `Result is rounded to nearest tick`() {
        // 14 + 7 * sqrt(14) / 0.6 = 57.65
        assertEquals(58, ResourceRespawn.ticks(14, players = 0))
    }

    @Test
    fun `Zero base is coerced to one tick`() {
        assertEquals(1, ResourceRespawn.ticks(0, players = 0))
        assertEquals(1, ResourceRespawn.ticks(0, players = 2048))
    }

    @Test
    fun `Runite at low population`() {
        // 1000 + 7 * sqrt(1000) / 0.6 = 1368.97
        assertEquals(1369, ResourceRespawn.ticks(1000, players = 0))
    }
}
