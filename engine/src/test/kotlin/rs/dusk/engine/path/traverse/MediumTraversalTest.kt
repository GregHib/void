package rs.dusk.engine.path.traverse

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.CollisionFlag
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.clear

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
internal class MediumTraversalTest {

    lateinit var collisions: Collisions
    lateinit var traversal: MediumTraversal

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        collisions = mockk(relaxed = true)
        traversal = spyk(MediumTraversal(collisions))
    }

    /**
     * |X| | |
     * |E|E| |
     * |E|E| |
     */
    @Test
    fun `North blocked at the start`() {
        // Given
        val start = Tile(1, 1)
        every { collisions.check(start.x, start.y + 2, start.plane, CollisionFlag.LAND_BLOCK_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | |X| |
     * |E|E| |
     * |E|E| |
     */
    @Test
    fun `North blocked at the end`() {
        // Given
        val start = Tile(1, 1)
        every {
            collisions.check(
                start.x + 1,
                start.y + 2,
                start.plane,
                CollisionFlag.LAND_BLOCK_SOUTH_WEST
            )
        } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E| |
     * | | |X|
     */
    @Test
    fun `South-east blocked diagonally`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collisions.check(any(), any(), any(), it.clear()) } returns true
        }
        every {
            collisions.check(
                start.x + 2,
                start.y - 1,
                start.plane,
                CollisionFlag.LAND_WALL_NORTH_WEST
            )
        } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E| |
     * | |X| |
     */
    @Test
    fun `South-east blocked vertically`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collisions.check(any(), any(), any(), it.clear()) } returns true
        }
        every { collisions.check(start.x + 1, start.y - 1, start.plane, CollisionFlag.LAND_CLEAR_NORTH) } returns false
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E|X|
     * | | | |
     */
    @Test
    fun `South-east blocked horizontally`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collisions.check(any(), any(), any(), it.clear()) } returns true
        }
        every { collisions.check(start.x + 2, start.y, start.plane, CollisionFlag.LAND_CLEAR_WEST) } returns false
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

}