package rs.dusk.engine.path.algorithm

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Steps
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 22, 2020
 */
internal class DirectSearchTest {

    lateinit var ds: DirectSearch

    @BeforeEach
    fun setup() {
        ds = spyk(DirectSearch())
    }

    val size = Size.TILE

    @TestFactory
    fun `Horizontal moves towards target`() = arrayOf(
        Tile(11, 10) to Direction.WEST,
        Tile(9, 10) to Direction.EAST
    ).map { (tile, dir) ->
        dynamicTest("Horizontal moves $dir if $tile of target") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            every { strategy.tile } returns target
            every { strategy.reached(target, size) } returns true
            // When
            val result = ds.addHorizontal(steps, tile, size, strategy, traversal)
            // Then
            result as PathResult.Success.Complete
            assertEquals(target, result.last)
            verify {
                steps.add(dir)
            }
        }
    }

    @TestFactory
    fun `Horizontal doesn't move if blocked`() = arrayOf(
        Tile(11, 10) to Direction.WEST,
        Tile(9, 10) to Direction.EAST
    ).map { (tile, dir) ->
        dynamicTest("Horizontal blocked $dir") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            every { strategy.tile } returns target
            every { traversal.blocked(tile.x, tile.y, tile.plane, dir) } returns true
            // When
            val result = ds.addHorizontal(steps, tile, size, strategy, traversal)
            // Then
            result as PathResult.Success.Partial
            verify(exactly = 0) {
                steps.add(dir)
            }
        }
    }

    @Test
    fun `Horizontal moves none if aligned with target`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 11)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { strategy.reached(target, size) } returns true
        // When
        val result = ds.addHorizontal(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(target, result.last)
        verify(exactly = 0) {
            steps.add(Direction.WEST)
            steps.add(Direction.EAST)
        }
    }

    @Test
    fun `Horizontal returns partial if vertical blocked`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 11)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { traversal.blocked(tile, any()) } returns true
        // When
        val result = ds.addHorizontal(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Partial
        assertEquals(tile, result.last)
        verify(exactly = 0) {
            steps.add(Direction.WEST)
            steps.add(Direction.EAST)
        }
    }

    @Test
    fun `Horizontal moves vertical if not blocked`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(10, 11)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { ds.addVertical(steps, any(), size, strategy, traversal) } returns PathResult.Success.Complete(tile)
        // When
        val result = ds.addHorizontal(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(tile, result.last)
        verify {
            ds.addVertical(steps, any(), size, strategy, traversal)
        }
    }

    @TestFactory
    fun `Vertical moves towards target`() = arrayOf(
        Tile(10, 11) to Direction.SOUTH,
        Tile(10, 9) to Direction.NORTH
    ).map { (tile, dir) ->
        dynamicTest("Horizontal moves $dir if $tile of target") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            every { strategy.tile } returns target
            every { strategy.reached(target, size) } returns true
            // When
            val result = ds.addVertical(steps, tile, size, strategy, traversal)
            // Then
            result as PathResult.Success.Complete
            assertEquals(target, result.last)
            verify {
                steps.add(dir)
            }
        }
    }

    @TestFactory
    fun `Vertical doesn't move if blocked`() = arrayOf(
        Tile(10, 11) to Direction.SOUTH,
        Tile(10, 9) to Direction.NORTH
    ).map { (tile, dir) ->
        dynamicTest("Horizontal blocked $dir") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            every { strategy.tile } returns target
            every { traversal.blocked(tile.x, tile.y, tile.plane, dir) } returns true
            // When
            val result = ds.addVertical(steps, tile, size, strategy, traversal)
            // Then
            result as PathResult.Success.Partial
            verify(exactly = 0) {
                steps.add(dir)
            }
        }
    }


    @Test
    fun `Vertical moves none if aligned with target`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 10)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { strategy.reached(target, size) } returns true
        // When
        val result = ds.addVertical(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(target, result.last)
        verify(exactly = 0) {
            steps.add(Direction.SOUTH)
            steps.add(Direction.NORTH)
        }
    }

    @Test
    fun `Vertical returns partial if horizontal blocked`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 10)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { traversal.blocked(tile, any()) } returns true
        // When
        val result = ds.addVertical(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Partial
        assertEquals(tile, result.last)
        verify(exactly = 0) {
            steps.add(Direction.SOUTH)
            steps.add(Direction.NORTH)
        }
    }

    @Test
    fun `Vertical moves horizontal if not blocked`() {
        // Given
        val steps: Steps = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val target = Tile(11, 10)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.tile } returns target
        every { ds.addHorizontal(steps, any(), size, strategy, traversal) } returns PathResult.Success.Complete(tile)
        // When
        val result = ds.addVertical(steps, tile, size, strategy, traversal)
        // Then
        result as PathResult.Success.Complete
        assertEquals(tile, result.last)
        verify {
            ds.addHorizontal(steps, any(), size, strategy, traversal)
        }
    }
}