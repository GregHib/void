package rs.dusk.engine.path.find

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Steps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.block
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 22, 2020
 */
internal class AxisAlignmentTest {

    lateinit var collisions: Collisions
    lateinit var aa: AxisAlignment

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        aa = spyk(AxisAlignment(collisions))
    }

    @Test
    fun `Horizontal steps break when reached`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 2)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { strategy.reached(12, 15, 0, size) } returns true
        // When
        val result = aa.horizontal(steps, tile, size, Direction.EAST, Direction.NONE, strategy)
        // Then
        verify(exactly = 2) {
            steps.add(Direction.EAST)
        }
        assert(result is PathResult.Success.Complete)
        result as PathResult.Success.Complete
        assertEquals(Tile(12, 15), result.last)
    }

    @Test
    fun `Horizontal break when collided`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 2)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { collisions.check(12, 15, 0, Direction.EAST.block()) } returns true
        // When
        val result = aa.horizontal(steps, tile, size, Direction.EAST, Direction.NONE, strategy)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(12, 15), result.last)
        verify {
            steps.add(Direction.EAST)
        }
    }

    @Test
    fun `Horizontal attempts vertical if not reached target`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { aa.vertical(steps, any(), size, any(), any(), strategy) } returns PathResult.Success.Partial(tile)
        every { collisions.check(12, 16, 0, Direction.EAST.block()) } returns false
        // When
        val result = aa.horizontal(steps, tile, size, Direction.NONE, Direction.NORTH, strategy)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(tile, result.last)
        verify {
            aa.vertical(steps, tile, size, any(), any(), strategy)
        }
    }

    @Test
    fun `Vertical steps break when reached`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 2)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { strategy.reached(10, 13, 0, size) } returns true
        // When
        val result = aa.horizontal(steps, tile, size, Direction.NONE, Direction.SOUTH, strategy)
        // Then
        verify(exactly = 2) {
            steps.add(Direction.SOUTH)
        }
        assert(result is PathResult.Success.Complete)
        result as PathResult.Success.Complete
        assertEquals(Tile(10, 13), result.last)
    }

    @Test
    fun `Vertical break when collided`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 2)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { collisions.check(10, 13, 0, Direction.SOUTH.block()) } returns true
        // When
        val result = aa.horizontal(steps, tile, size, Direction.NONE, Direction.SOUTH, strategy)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(10, 13), result.last)
        verify {
            steps.add(Direction.SOUTH)
        }
    }

    @Test
    fun `Vertical attempts horizontal if not reached target`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 15)
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { aa.horizontal(steps, any(), size, any(), any(), strategy) } returns PathResult.Success.Partial(tile)
        every { collisions.check(9, 15, 0, Direction.WEST.block()) } returns false
        // When
        val result = aa.horizontal(steps, tile, size, Direction.WEST, Direction.NONE, strategy)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(tile, result.last)
        verify {
            aa.horizontal(steps, tile, size, any(), any(), strategy)
        }
    }

    @Test
    fun `Direction from delta`() {
        Direction.values().forEach {
            val delta = Tile(it.delta.x * 10, it.delta.y * 10)
            val direction = aa.toDirection(delta)
            assertEquals(it, direction)
        }
    }
}