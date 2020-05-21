package rs.dusk.engine.path.obstruction

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.get
import org.koin.dsl.module
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_CLEAR_EAST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_CLEAR_NORTH
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_CLEAR_SOUTH
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_CLEAR_WEST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_WALL_NORTH_EAST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_WALL_NORTH_WEST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_WALL_SOUTH_EAST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND_WALL_SOUTH_WEST
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.clear
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
internal class LargeObstructionTest : KoinMock() {

    lateinit var collisions: Collisions
    lateinit var obstruction: LargeObstruction

    override val modules = listOf(module { single { mockk<Collisions>(relaxed = true) } })

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        collisions = get()
        Direction.all.forEach {
            every { collisions.check(any(), any(), any(), it.clear()) } returns true
        }
    }

    /**
     * |X| | |
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North obstructed at the start`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x, start.y + 1, start.plane, LAND_WALL_SOUTH_EAST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | |X| |
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North obstructed in the middle`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 1, start.y + 1, start.plane, LAND_CLEAR_SOUTH) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | | |X|
     * |E|E|E|
     * | | | |
     */
    @Test
    fun `North obstructed at the end`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 1)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 2, start.y + 1, start.plane, LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH)
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
    fun `North-east obstructed diagonally`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 3, start.y + 3, start.plane, LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH_EAST)
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
    fun `North-east obstructed vertically`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + offset, start.y + 3, start.plane, LAND_CLEAR_SOUTH) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH_EAST)
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
    fun `North-east obstructed horizontally`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(3, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 3, start.y + offset, start.plane, LAND_CLEAR_WEST) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.NORTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E| | |
     * |E| | |
     * |E|X| |
     */
    @Test
    fun `East obstructed at the start`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 1, start.y, start.plane, LAND_WALL_NORTH_WEST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E| | |
     * |E|X| |
     * |E| | |
     */
    @Test
    fun `East obstructed in the middle`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 1, start.y + 1, start.plane, LAND_CLEAR_WEST) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|X| |
     * |E| | |
     * |E| | |
     */
    @Test
    fun `East obstructed at the end`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(1, 3)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x + 1, start.y + 2, start.plane, LAND_WALL_SOUTH_WEST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.EAST)
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
    fun `South-west obstructed diagonally`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x - 1, start.y - 1, start.plane, LAND_WALL_NORTH_EAST) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.SOUTH_WEST)
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
    fun `South-west obstructed vertically`() {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x, start.y - 1, start.plane, LAND_CLEAR_NORTH) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.SOUTH_WEST)
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
    fun `South-west obstructed horizontally`(offset: Int) {
        // Given
        val start = Tile(1, 1)
        val size = Size(2, 4)
        obstruction = spyk(LargeObstruction(size))
        every { collisions.check(start.x - 1, start.y + offset, start.plane, LAND_CLEAR_EAST) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, Direction.SOUTH_WEST)
        // Then
        assertTrue(result)
    }
}