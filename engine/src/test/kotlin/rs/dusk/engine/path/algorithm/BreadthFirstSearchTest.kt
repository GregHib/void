package rs.dusk.engine.path.algorithm

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.entity.character.move.Steps
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.algorithm.BreadthFirstSearch.Companion.GRAPH_SIZE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
internal class BreadthFirstSearchTest {

    lateinit var bfs: BreadthFirstSearch

    @BeforeEach
    fun setup() {
        bfs = spyk(BreadthFirstSearch())
    }

    @Test
    fun `Movement reset`() {
        // Given
        val tile = Tile(0, 0)
        val size = Size(1, 1)
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        bfs.directions.fill(Array(GRAPH_SIZE) { Direction.NONE })
        every { bfs.calculate(any(), any(), any(), any(), any(), any()) } returns PathResult.Success.Complete(
            tile
        )
        every { bfs.backtrace(any(), any(), 0, 0) } returns PathResult.Success.Complete(tile)
        // When
        bfs.find(tile, size, movement, strategy, traversal)
        // Then
        assertEquals(null, bfs.directions[1][2])
        assertEquals(99999999, bfs.distances[3][4])
    }

    @Test
    fun `Partial path calculated if no complete path found`() {
        // Given
        val tile = Tile(0, 0)
        val size = Size(1, 1)
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val response = PathResult.Success.Complete(Tile(0, 0))
        every {
            bfs.calculate(
                any(),
                any(),
                tile.plane,
                size,
                strategy,
                traversal
            )
        } returns PathResult.Failure
        every { bfs.calculatePartialPath(strategy, any(), any()) } returns response
        // When
        bfs.find(tile, size, movement, strategy, traversal)
        // Then
        verify { bfs.calculatePartialPath(strategy, any(), any()) }
    }

    @Test
    fun `Finds route to target`() {
        // Given
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.reached(72, 74, 1, size) } returns true
        // When
        val result = bfs.calculate(10, 10, 1, size, strategy, traversal)
        // Then
        verifyOrder {
            strategy.reached(74, 74, 1, size)
            strategy.reached(73, 74, 1, size)// West
            strategy.reached(75, 74, 1, size)// East
            strategy.reached(74, 73, 1, size)// South
            strategy.reached(74, 75, 1, size)// North
            strategy.reached(73, 73, 1, size)// South west
            strategy.reached(75, 73, 1, size)// South east
            strategy.reached(73, 75, 1, size)// North west
            strategy.reached(75, 75, 1, size)// North east
        }
        assertEquals(0, bfs.distances[64][64])
        assertEquals(Direction.NONE, bfs.directions[64][64])
    }

    @Test
    fun `Obstructions are ignored`() {
        // Given
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { strategy.reached(73, 74, 1, size) } returns true
        every { traversal.blocked(73, 74, 1, Direction.WEST) } returns true
        // When
        val result = bfs.calculate(10, 10, 1, size, strategy, traversal)
        // Then
        assertFalse(bfs.calc.contains(Tile(73, 74, 1)))
        assert(bfs.calc.contains(Tile(65, 64)))
    }

    @Test
    fun `Partial calculation takes lowest cost`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        bfs.distances[5][5] = 2
        bfs.distances[4][5] = 3
        bfs.distances[3][5] = 4
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(strategy, 10, 10)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(3, 5), result.last)
    }

    @Test
    fun `Partial calculation takes lowest distance`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        bfs.distances[5][5] = 2
        bfs.distances[6][6] = 2
        bfs.distances[7][7] = 2
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(strategy, 10, 10)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(5, 5), result.last)
    }

    @Test
    fun `Partial calculation returns failure if no values`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(strategy, 10, 10)
        // Then
        assert(result is PathResult.Failure)
    }

    @Test
    fun `Backtrace steps`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val result = PathResult.Success.Complete(tile)
        bfs.directions[10][10] = Direction.NORTH
        bfs.directions[10][9] = Direction.EAST
        bfs.directions[9][9] = Direction.SOUTH
        bfs.directions[9][10] = Direction.SOUTH
        bfs.directions[9][11] = Direction.WEST
        bfs.directions[10][11] = Direction.NONE
        val steps: Steps = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        // When
        bfs.backtrace(movement, result, 0, 0)
        // Then
        verifyOrder {
            steps.add(1, Direction.NORTH)
            steps.add(1, Direction.EAST)
            steps.add(1, Direction.SOUTH)
            steps.add(1, Direction.SOUTH)
            steps.add(1, Direction.WEST)
        }
    }

    @Test
    fun `Backtrace returns failure if no movement`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val steps: Steps = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        // When
        val result = bfs.backtrace(movement, PathResult.Success.Complete(tile), 0, 0)
        // Then
        assert(result is PathResult.Failure)
    }
}