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
        collisions = spyk(Collisions())
    }

    @Test
    fun `Clear a chunk`() {
        // Given
        for (i in 0 until 8) {
            set(i, i, 0, CollisionFlag.BLOCK_NORTH)
        }
        // When
        collisions.clear(Chunk(0, 0))
        // Then
        for (i in 0 until 8) {
            assertEquals(0, i, i, 0)
        }
    }

    private fun print(chunk: Chunk) {
        val data = collisions.allocateIfAbsent(chunk.tile.x, chunk.tile.y, chunk.plane)
        for (y in 7 downTo 0) {
            for (x in 0 until 8) {
                print("${data[(chunk.tile.x + x) + ((chunk.tile.y + y) shl 3)]} ")
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