package world.gregs.voidps.engine.map

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Overlap.isUnder
import world.gregs.voidps.type.Direction

internal class OverlapTest {

    @Test
    fun `1x1 under 1x1`() {
        assertTrue(isUnder(x = 123, y = 432, width = 1, height = 1, targetX = 123, targetY = 432, targetWidth = 1, targetHeight = 1))
    }

    @Test
    fun `1x1 beside 1x1`() {
        for (dir in Direction.all) {
            assertFalse(isUnder(x = 123, y = 432, width = 1, height = 1, targetX = 123 + dir.delta.x, targetY = 433 + dir.delta.x, targetWidth = 1, targetHeight = 1))
        }
    }

    @Test
    fun `1x1 under 2x2`() {
        assertTrue(isUnder(x = 124, y = 457, width = 1, height = 1, targetX = 123, targetY = 456, targetWidth = 2, targetHeight = 2))
    }

    @Test
    fun `2x2 overlaps 2x2`() {
        assertTrue(isUnder(x = 101, y = 101, width = 2, height = 2, targetX = 100, targetY = 100, targetWidth = 2, targetHeight = 2))
    }
}
