package rs.dusk.engine.path.find

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.Movement
import rs.dusk.engine.model.entity.character.Steps
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

internal class AxisAlignmentTest {

    lateinit var aa: AxisAlignment
    val size = Size.TILE

    @BeforeEach
    fun setup() {
        aa = spyk(AxisAlignment())
    }

    @Test
    fun `Already reached target is complete`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(0, 0)
        val target = Tile(0, 0)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.tile } returns target
        every { strategy.reached(target, size) } returns true
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(target, result.last)
    }

    @Test
    fun `Unreached no steps towards target is failure`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 10)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.tile } returns target
        every { strategy.reached(target, size) } returns false
        every { aa.toDirection(any()) } returns Direction.NONE
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        assert(result is PathResult.Failure)
    }

    @Test
    fun `Diagonal blocked tries horizontal`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 10)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.reached(target, size) } returns true
        every { aa.toDirection(any()) } returns Direction.SOUTH_EAST
        every { traversal.blocked(tile, Direction.SOUTH_EAST) } returns true
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(target, result.last)
        verify {
            traversal.blocked(tile, Direction.EAST)
            steps.add(Direction.EAST)
        }
    }

    @Test
    fun `Diagonal and horizontal blocked tries vertical`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 9)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.reached(target, size) } returns true
        every { aa.toDirection(any()) } returns Direction.SOUTH_EAST
        every { traversal.blocked(tile, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(tile, Direction.EAST) } returns true
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(target, result.last)
        verify {
            traversal.blocked(tile, Direction.SOUTH)
            steps.add(Direction.SOUTH)
        }
    }

    @Test
    fun `Blocked route is failure`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 9)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.reached(target, size) } returns true
        every { aa.toDirection(any()) } returns Direction.SOUTH_EAST
        every { traversal.blocked(tile, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(tile, Direction.EAST) } returns true
        every { traversal.blocked(tile, Direction.SOUTH) } returns true
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        assert(result is PathResult.Failure)
        verify(exactly = 0) {
            steps.add(any())
        }
    }

    @Test
    fun `Blocked route is partial`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(9, 11)
        val target = Tile(11, 9)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { strategy.reached(target, size) } returns true
        every { aa.toDirection(any()) } returns Direction.SOUTH_EAST
        every { traversal.blocked(tile, Direction.SOUTH_EAST) } returns false
        val blocked = tile.add(Direction.SOUTH_EAST.delta)
        every { traversal.blocked(blocked, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(blocked, Direction.EAST) } returns true
        every { traversal.blocked(blocked, Direction.SOUTH) } returns true
        // When
        val result = aa.find(tile, size, movement, strategy, traversal)
        // Then
        result as PathResult.Success.Partial
        assertEquals(blocked, result.last)
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