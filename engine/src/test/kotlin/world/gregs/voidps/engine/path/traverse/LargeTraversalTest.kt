package world.gregs.voidps.engine.path.traverse

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import org.koin.test.get
import world.gregs.voidps.engine.TestFlags
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag.PLAYER
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.strategy.CharacterCollision
import world.gregs.voidps.engine.map.collision.strategy.IgnoredCollision
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.collision.strategy.SkyCollision
import world.gregs.voidps.engine.script.KoinMock

internal class LargeTraversalTest : KoinMock() {

    lateinit var collisions: Collisions
    lateinit var traversal: LargeTraversal
    lateinit var collision: CollisionStrategy

    override val modules = listOf(module { single { mockk<Collisions>(relaxed = true) } })

    @BeforeEach
    fun setup() {
        collisions = get()
        every { collisions.check(any(), any(), any(), any()) } returns true
        traversal = spyk(LargeTraversal)
        collision = LandCollision(collisions)
    }

    /**
     * |X| | |
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North blocked at the start`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        every { collisions.check(start.x, start.y + 1, start.plane, TestFlags.LAND_WALL_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(
            collision = LandCollision(collisions), start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | |X| |
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North blocked in the middle`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        every { collisions.check(start.x + 1, start.y + 1, start.plane, TestFlags.LAND_CLEAR_SOUTH) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | | |X|
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North blocked at the end`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        every { collisions.check(start.x + 2, start.y + 1, start.plane, TestFlags.LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | | | |X|
     * |E|E|E| |
     * |E|E|E| |
     * |E|E|E| |
     */
    @Test
    fun `North-east blocked diagonally`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        every { collisions.check(start.x + 3, start.y + 3, start.plane, TestFlags.LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * | |X|X| |
     * |E|E|E| |
     * |E|E|E| |
     * |E|E|E| |
     */
    @ParameterizedTest
    @ValueSource(ints = [1, 2])
    fun `North-east blocked vertically`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        every { collisions.check(start.x + offset, start.y + 3, start.plane, TestFlags.LAND_CLEAR_SOUTH) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * | | | | |
     * |E|E|E|X|
     * |E|E|E|X|
     * |E|E|E| |
     */
    @ParameterizedTest
    @ValueSource(ints = [1, 2])
    fun `North-east blocked horizontally`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        every { collisions.check(start.x + 3, start.y + offset, start.plane, TestFlags.LAND_CLEAR_WEST) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E| | |
     * |E| | |
     * |E|X| |
     */
    @Test
    fun `East blocked at the start`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        every { collisions.check(start.x + 1, start.y, start.plane, TestFlags.LAND_WALL_NORTH_WEST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E| | |
     * |E|X| |
     * |E| | |
     */
    @Test
    fun `East blocked in the middle`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        every { collisions.check(start.x + 1, start.y + 1, start.plane, TestFlags.LAND_CLEAR_WEST) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|X| |
     * |E| | |
     * |E| | |
     */
    @Test
    fun `East blocked at the end`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        every { collisions.check(start.x + 1, start.y + 2, start.plane, TestFlags.LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.EAST)
        // Then
        assertTrue(result)
    }

    /**
     * | |E|E|
     * | |E|E|
     * | |E|E|
     * | |E|E|
     * |X| | |
     */
    @Test
    fun `South-west blocked diagonally`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        every { collisions.check(start.x - 1, start.y - 1, start.plane, TestFlags.LAND_WALL_NORTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.SOUTH_WEST)
        // Then
        assertTrue(result)
    }

    /**
     * | |E|E|
     * | |E|E|
     * | |E|E|
     * | |E|E|
     * | |X| |
     */
    @Test
    fun `South-west blocked vertically`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        every { collisions.check(start.x, start.y - 1, start.plane, TestFlags.LAND_CLEAR_NORTH) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.SOUTH_WEST)
        // Then
        assertTrue(result)
    }

    /**
     * | |E|E|
     * |X|E|E|
     * |X|E|E|
     * |X|E|E|
     * | | | |
     */
    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `South-west blocked horizontally`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        every { collisions.check(start.x - 1, start.y + offset, start.plane, TestFlags.LAND_CLEAR_EAST) } returns false
        // When
        val result = traversal.blocked(collision, start, size, Direction.SOUTH_WEST)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by entity`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        collision = CharacterCollision(collisions)
        every { collisions.check(start.x, start.y + 1, start.plane, TestFlags.LAND_WALL_SOUTH_EAST or PLAYER) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by sky`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        collision = SkyCollision(collisions)
        every { collisions.check(start.x, start.y + 1, start.plane, TestFlags.SKY_WALL_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Blocked by ignored`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        collision = IgnoredCollision(collisions, LandCollision(collisions))
        every { collisions.check(start.x, start.y + 1, start.plane, TestFlags.IGNORED_WALL_SOUTH_EAST) } returns true
        // When
        val result = traversal.blocked(collision, start, size, Direction.NORTH)
        // Then
        assertTrue(result)
    }
}