package rs.dusk.engine.path.traverse

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.model.map.collision.CollisionFlag
import rs.dusk.engine.model.map.collision.CollisionFlag.ENTITY
import rs.dusk.engine.model.map.collision.CollisionFlag.LAND_BLOCK_SOUTH_EAST
import rs.dusk.engine.model.map.collision.CollisionFlag.LAND_BLOCK_SOUTH_WEST
import rs.dusk.engine.model.map.collision.CollisionFlag.LAND_CLEAR_NORTH
import rs.dusk.engine.model.map.collision.CollisionFlag.LAND_CLEAR_WEST
import rs.dusk.engine.model.map.collision.CollisionFlag.LAND_WALL_NORTH_WEST
import rs.dusk.engine.model.map.collision.CollisionFlag.SKY_BLOCK_SOUTH_EAST
import rs.dusk.engine.model.map.collision.Collisions
import rs.dusk.engine.model.map.collision.check
import rs.dusk.engine.path.TraversalType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
internal class MediumTraversalTest {

    lateinit var collisions: Collisions
    lateinit var traversal: MediumTraversal

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.model.world.collision.CollisionsKt")
        collisions = mockk(relaxed = true)
        traversal = spyk(MediumTraversal(TraversalType.Land, false, collisions))
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
        every { collisions.check(start.x, start.y + 2, start.plane, LAND_BLOCK_SOUTH_EAST) } returns true
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
        every { collisions.check(start.x + 1, start.y + 2, start.plane, LAND_BLOCK_SOUTH_WEST) } returns true
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
        every { collisions.check(any(), any(), any(), any()) } returns true
        every { collisions.check(start.x + 2, start.y - 1, start.plane, LAND_WALL_NORTH_WEST) } returns true
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
        every { collisions.check(any(), any(), any(), any()) } returns true
        every { collisions.check(start.x + 1, start.y - 1, start.plane, LAND_CLEAR_NORTH) } returns false
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
        every { collisions.check(any(), any(), any(), any()) } returns true
        every { collisions.check(start.x + 2, start.y, start.plane, LAND_CLEAR_WEST) } returns false
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    @Test
    fun `North blocked by entity`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal(TraversalType.Land, true, collisions))
        every { collisions.check(start.x, start.y + 2, start.plane, LAND_BLOCK_SOUTH_EAST or ENTITY) } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by sky`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal(TraversalType.Sky, false, collisions))
        every { collisions.check(start.x, start.y + 2, start.plane, SKY_BLOCK_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by ignored`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal(TraversalType.Ignored, false, collisions))
        every { collisions.check(start.x, start.y + 2, start.plane, CollisionFlag.IGNORED_BLOCK_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

}