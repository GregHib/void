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

    @Test
    fun `Removing collision from an unallocated zone leaves it blocked`() {
        // When
        GameObjectCollisionRemove().modifyTile(3, 3, 0, 0, 1)
        // Then
        assertEquals(-1, 3, 3, 0)
        assertEquals(false, Collisions.isZoneAllocated(3, 3, 0))
    }

    @Test
    fun `Move flag within a zone`() {
        set(1, 1, 0, CollisionFlag.BLOCK_NPCS or CollisionFlag.FLOOR)
        set(2, 1, 0, 0)

        Collisions.move(1, 1, 0, 2, 1, 0, CollisionFlag.BLOCK_NPCS)

        assertEquals(CollisionFlag.FLOOR, 1, 1, 0)
        assertEquals(CollisionFlag.BLOCK_NPCS, 2, 1, 0)
    }

    @Test
    fun `Move flag into another zone`() {
        set(7, 7, 0, CollisionFlag.BLOCK_NPCS)
        set(8, 8, 1, 0)

        Collisions.move(7, 7, 0, 8, 8, 1, CollisionFlag.BLOCK_NPCS)

        assertEquals(0, 7, 7, 0)
        assertEquals(CollisionFlag.BLOCK_NPCS, 8, 8, 1)
    }

    @Test
    fun `Moving from an unallocated zone leaves it unallocated`() {
        set(8, 0, 0, 0)

        Collisions.move(7, 0, 0, 8, 0, 0, CollisionFlag.BLOCK_NPCS)

        assertEquals(false, Collisions.isZoneAllocated(7, 0, 0))
        assertEquals(CollisionFlag.BLOCK_NPCS, 8, 0, 0)
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
