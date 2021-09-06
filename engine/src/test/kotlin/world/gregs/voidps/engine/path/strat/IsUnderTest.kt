package world.gregs.voidps.engine.path.strat

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isUnder

internal class IsUnderTest {

    @Test
    fun `1x1 under 1x1`() {
        assertTrue(isUnder(123, 432, Size.TILE, 123, 432, Size.TILE))
    }

    @Test
    fun `1x1 beside 1x1`() {
        for (dir in Direction.all) {
            assertFalse(isUnder(123, 432, Size.TILE, 123 + dir.delta.x, 433 + dir.delta.x, Size.TILE))
        }
    }

    @Test
    fun `1x1 under 2x2`() {
        assertTrue(isUnder(124, 457, Size.TILE, 123, 456, Size(2, 2)))
    }

    @Test
    fun `2x2 overlaps 2x2`() {
        assertTrue(isUnder(101, 101, Size(2, 2), 100, 100, Size(2, 2)))
    }
}