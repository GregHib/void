package world.gregs.voidps.engine.path.traverse

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.NPCCollision
import world.gregs.voidps.engine.map.collision.check

internal class SmallTraversalTest {

    lateinit var collisions: Collisions
    lateinit var traversal: SmallTraversal
    lateinit var collision: CollisionStrategy

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionsKt")
        collisions = mockk(relaxed = true)
        traversal = spyk(SmallTraversal)
        collision = NPCCollision(collisions)
    }

    @Test
    fun `Blocked cardinal`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH
        val tile = start.add(direction.delta)
        every { collisions.check(tile.x, tile.y, tile.plane, any()) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Clear cardinal`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH
        every { collisions.check(1, 2, 0, any()) } returns false
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Diagonal blocked diagonally`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH_EAST
        every { collisions.check(2, 2, 0, any()) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal blocked horizontally`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH_EAST
        every { collisions.check(2, 1, 0, any()) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal blocked vertically`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        every { collisions.check(1, 0, 0, any()) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal clear`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        every { collisions.check(any(), any(), any(), any()) } returns false
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Blocked by entities`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        traversal = spyk(SmallTraversal)
        every { collisions.check(any(), any(), any(), CollisionFlag.LAND_BLOCK_NORTH_EAST or CollisionFlag.PLAYER) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Not blocked by entities`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        traversal = spyk(SmallTraversal)
        every { collisions.check(any(), any(), any(), CollisionFlag.LAND_BLOCK_NORTH_EAST or CollisionFlag.PLAYER) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Blocked sky`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        traversal = spyk(SmallTraversal)
        every { collisions.check(any(), any(), any(), CollisionFlag.SKY_BLOCK_NORTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked ignored`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        traversal = spyk(SmallTraversal)
        every { collisions.check(any(), any(), any(), CollisionFlag.IGNORED_BLOCK_NORTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertTrue(result)
    }
}