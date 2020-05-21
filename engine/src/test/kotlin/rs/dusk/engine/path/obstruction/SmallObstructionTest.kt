package rs.dusk.engine.path.obstruction

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.block
import rs.dusk.engine.model.world.map.collision.check

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
internal class SmallObstructionTest {

    lateinit var collisions: Collisions
    lateinit var obstruction: SmallObstruction

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        collisions = mockk(relaxed = true)
        obstruction = spyk(SmallObstruction(collisions))
    }

    @Test
    fun `Obstructed cardinal`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH
        val tile = start.add(direction.delta)
        every { collisions.check(tile.x, tile.y, tile.plane, direction.inverse().block()) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Unobstructed cardinal`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH
        every { collisions.check(1, 2, 0, any()) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Diagonal obstructed diagonally`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH_EAST
        every { collisions.check(2, 2, 0, any()) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal obstructed horizontally`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.NORTH_EAST
        every { collisions.check(2, 1, 0, any()) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal obstructed vertically`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        every { collisions.check(1, 0, 0, any()) } returns true
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Diagonal unobstructed`() {
        // Given
        val start = Tile(1, 1)
        val direction = Direction.SOUTH_WEST
        every { collisions.check(any(), any(), any(), any()) } returns false
        // When
        val result = obstruction.obstructed(start.x, start.y, start.plane, direction)
        // Then
        assertFalse(result)
    }
}