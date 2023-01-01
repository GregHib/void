package world.gregs.voidps.engine.path.algorithm

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.engine.value
import java.util.*

internal class AxisAlignmentTest {

    lateinit var aa: AxisAlignment
    val size = Size.ONE

    @BeforeEach
    fun setup() {
        aa = spyk(AxisAlignment())
    }

    @Test
    fun `Already reached target is complete`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(0, 0)
        val target = Tile(0, 0)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        val path: Path = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.tile } returns value(target)
        every { strategy.reached(target, size) } returns true
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        result as PathResult.Success
        assertEquals(target, result.last)
    }

    @Test
    fun `Unreached no steps towards target is failure`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 10)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        val path: Path = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.tile } returns value(target)
        every { strategy.reached(target, size) } returns false
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        assert(result is PathResult.Failure)
    }

    @Test
    fun `Diagonal blocked tries horizontal`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 10)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        every { strategy.tile } returns value(target)
        val path: Path = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.reached(target, size) } returns true
        every { traversal.blocked(collision, tile, size, Direction.SOUTH_EAST) } returns true
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        result as PathResult.Success
        assertEquals(target, result.last)
        verify {
            traversal.blocked(collision, tile, size, Direction.EAST)
            steps.add(Direction.EAST)
        }
    }

    @Test
    fun `Diagonal and horizontal blocked tries vertical`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 9)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        every { strategy.tile } returns value(target)
        val path: Path = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.reached(target, size) } returns true
        every { traversal.blocked(collision, tile, size, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(collision, tile, size, Direction.EAST) } returns true
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        result as PathResult.Success
        assertEquals(target, result.last)
        verify {
            traversal.blocked(collision, tile, size, Direction.SOUTH)
            steps.add(Direction.SOUTH)
        }
    }

    @Test
    fun `Blocked route is failure`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 9)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        val path: Path = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.reached(target, size) } returns true
        every { strategy.tile } returns value(target)
        every { traversal.blocked(collision, tile, size, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(collision, tile, size, Direction.EAST) } returns true
        every { traversal.blocked(collision, tile, size, Direction.SOUTH) } returns true
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        assert(result is PathResult.Failure)
        verify(exactly = 0) {
            steps.add(any())
        }
    }

    @Test
    fun `Blocked route is partial`() {
        // Given
        val steps: LinkedList<Direction> = mockk(relaxed = true)
        val tile = Tile(9, 11)
        val target = Tile(11, 9)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val movement: Movement = mockk(relaxed = true)
        val path: Path = mockk(relaxed = true)
        val collision: CollisionStrategyOld = mockk(relaxed = true)
        every { path.steps } returns steps
        every { path.strategy } returns strategy
        every { movement.path } returns path
        every { strategy.reached(target, size) } returns true
        every { strategy.tile } returns value(target)
        every { traversal.blocked(collision, tile, size, Direction.SOUTH_EAST) } returns false
        val blocked = tile.add(Direction.SOUTH_EAST.delta)
        every { traversal.blocked(collision, blocked, size, Direction.SOUTH_EAST) } returns true
        every { traversal.blocked(collision, blocked, size, Direction.EAST) } returns true
        every { traversal.blocked(collision, blocked, size, Direction.SOUTH) } returns true
        // When
        val result = aa.find(tile, size, path, traversal, collision)
        // Then
        result as PathResult.Partial
        assertEquals(blocked, result.last)
    }

    @Test
    fun `Direction from delta`() {
        Direction.values().forEach {
            val delta = Delta(it.delta.x * 10, it.delta.y * 10)
            val direction = delta.toDirection()
            assertEquals(it, direction)
        }
    }
}