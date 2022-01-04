package world.gregs.voidps.engine.path.traverse

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.*
import world.gregs.voidps.engine.map.collision.CollisionFlag.LAND_BLOCK_SOUTH_EAST
import world.gregs.voidps.engine.map.collision.CollisionFlag.LAND_BLOCK_SOUTH_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.LAND_CLEAR_NORTH
import world.gregs.voidps.engine.map.collision.CollisionFlag.LAND_CLEAR_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.LAND_WALL_NORTH_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.PLAYER
import world.gregs.voidps.engine.map.collision.CollisionFlag.SKY_BLOCK_SOUTH_EAST

internal class MediumTraversalTest {

    lateinit var collisions: Collisions
    lateinit var traversal: MediumTraversal
    lateinit var collision: CollisionStrategy

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionsKt")
        collisions = mockk(relaxed = true)
        traversal = spyk(MediumTraversal)
        collision = PlayerCollision(collisions)
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
        val result = traversal.blocked(collision, start, Size.ONE, Direction.NORTH)
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
        val result = traversal.blocked(collision, start, Size.ONE, Direction.NORTH)
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
        val result = traversal.blocked(collision, start, Size.ONE, Direction.SOUTH_EAST)
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
        val result = traversal.blocked(collision, start, Size.ONE, Direction.SOUTH_EAST)
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
        val result = traversal.blocked(collision, start, Size.ONE, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    @Test
    fun `North blocked by entity`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal)
        collision = NPCCollision(collisions)
        every { collisions.check(start.x, start.y + 2, start.plane, LAND_BLOCK_SOUTH_EAST or PLAYER) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by sky`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal)
        collision = SkyCollision(collisions)
        every { collisions.check(start.x, start.y + 2, start.plane, SKY_BLOCK_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by ignored`() {
        // Given
        val start = Tile(1, 1)
        traversal = spyk(MediumTraversal)
        collision = IgnoredCollision(collisions)
        every { collisions.check(start.x, start.y + 2, start.plane, CollisionFlag.IGNORED_BLOCK_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, Direction.NORTH)
        // Then
        assertTrue(result)
    }

}