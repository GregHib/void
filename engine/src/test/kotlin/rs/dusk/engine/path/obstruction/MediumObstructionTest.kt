package rs.dusk.engine.path.obstruction

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
internal class MediumObstructionTest {

    lateinit var collision: Collisions
    lateinit var obstruction: MediumObstruction

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        collision = mockk(relaxed = true)
        obstruction = spyk(MediumObstruction(collision))
    }

    /**
     * |X| | |
     * |E|E| |
     * |E|E| |
     */
    @Test
    fun `North obstructed at the start`() {
        // Given
        val start = Tile(1, 1)
        every { collision.check(start.x, start.y + 2, start.plane, CollisionFlag.LAND_WALL_SOUTH_EAST) } returns true
        // When
        val result = obstruction.obstructed(start, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * | |X| |
     * |E|E| |
     * |E|E| |
     */
    @Test
    fun `North obstructed at the end`() {
        // Given
        val start = Tile(1, 1)
        every {
            collision.check(
                start.x + 1,
                start.y + 2,
                start.plane,
                CollisionFlag.LAND_WALL_SOUTH_WEST
            )
        } returns true
        // When
        val result = obstruction.obstructed(start, Direction.NORTH)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E| |
     * | | |X|
     */
    @Test
    fun `South-east obstructed diagonally`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collision.check(any(), any(), any(), it.clear()) } returns true
        }
        every {
            collision.check(
                start.x + 2,
                start.y - 1,
                start.plane,
                CollisionFlag.LAND_WALL_NORTH_WEST
            )
        } returns true
        // When
        val result = obstruction.obstructed(start, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E| |
     * | |X| |
     */
    @Test
    fun `South-east obstructed vertically`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collision.check(any(), any(), any(), it.clear()) } returns true
        }
        every { collision.check(start.x + 1, start.y - 1, start.plane, CollisionFlag.LAND_CLEAR_NORTH) } returns false
        // When
        val result = obstruction.obstructed(start, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

    /**
     * |E|E| |
     * |E|E|X|
     * | | | |
     */
    @Test
    fun `South-east obstructed horizontally`() {
        // Given
        val start = Tile(1, 1)
        Direction.all.forEach {
            every { collision.check(any(), any(), any(), it.clear()) } returns true
        }
        every { collision.check(start.x + 2, start.y, start.plane, CollisionFlag.LAND_CLEAR_WEST) } returns false
        // When
        val result = obstruction.obstructed(start, Direction.SOUTH_EAST)
        // Then
        assertTrue(result)
    }

}