package world.gregs.voidps.engine.map.collision

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.collision.CollisionFlagMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.map.chunk.Chunk

internal class CollisionsTest {

    lateinit var data: Array<IntArray?>
    lateinit var collisions: CollisionFlagMap

    @BeforeEach
    fun setup() {
        data = arrayOfNulls(2048 * 2048 * 4)
        collisions = spyk(Collisions(data))
    }

    @Test
    fun `Copy a rotated chunk`() {
        // Given
        for (i in 0 until 8) {
            set(i, 0, 0, CollisionFlag.BLOCK_NORTH)
        }
        // When
        collisions.copy(Chunk.EMPTY, Chunk.EMPTY, 3)
        // Then
        for (i in 0 until 8) {
            assertEquals(CollisionFlag.BLOCK_WEST, 7, i, 0)
        }
    }

    @Test
    fun `Copy a chunk to another plane`() {
        // Given
        for (i in 0 until 8) {
            set(i, i, 0, CollisionFlag.BLOCK_NORTH_EAST)
        }
        // When
        collisions.copy(Chunk.EMPTY, Chunk(1, 1, 1), 2)
        // Then
        for (i in 0 until 8) {
            assertEquals(CollisionFlag.BLOCK_SOUTH_WEST, 8 + i, 8 + i, 1)
        }
    }

    private fun print(chunk: Chunk) {
        val data = data[chunk.regionPlane.id]!!
        for (y in 7 downTo 0) {
            for (x in 0 until 8) {
                print("${data[((chunk.tile.x + x) * 64) + (chunk.tile.y + y)]} ")
            }
            println()
        }
        println()
    }

    private fun set(x: Int, y: Int, plane: Int, value: Int) {
        collisions[x, y, plane] = value
    }

    private fun assertEquals(expected: Int, x: Int, y: Int, plane: Int) {
        assertEquals(expected, collisions[x, y, plane]) { "x=$x, y=$y, plane=$plane" }
    }
}