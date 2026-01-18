package world.gregs.voidps.engine.map.collision

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.type.Zone

internal class CollisionsTest {

    @BeforeEach
    fun setup() {
        Collisions.clear()
    }

    @Test
    fun `Clear a zone`() {
        // Given
        for (i in 0 until 8) {
            set(i, i, 0, CollisionFlag.BLOCK_NORTH)
        }
        // When
        Collisions.clear(Zone(0, 0))
        // Then
        for (i in 0 until 8) {
            assertEquals(-1, i, i, 0)
        }
    }

    private fun print(zone: Zone) {
        val data = Collisions.allocateIfAbsent(zone.tile.x, zone.tile.y, zone.level)
        for (y in 7 downTo 0) {
            for (x in 0 until 8) {
                print("${data[(zone.tile.x + x) + ((zone.tile.y + y) shl 3)]} ")
            }
            println()
        }
        println()
    }

    private fun set(x: Int, y: Int, level: Int, value: Int) {
        Collisions[x, y, level] = value
    }

    private fun assertEquals(expected: Int, x: Int, y: Int, level: Int) {
        assertEquals(expected, Collisions[x, y, level]) { "x=$x, y=$y, level=$level" }
    }
}
