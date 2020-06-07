package rs.dusk.engine.path.find

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.entity.index.Steps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.find.BreadthFirstSearch.Companion.GRAPH_SIZE
import java.util.*

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
        val directions = Array(GRAPH_SIZE) { Array<Direction?>(GRAPH_SIZE) { Direction.NONE } }
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }
        every { movement.directions } returns directions
        every { movement.distances } returns distances
        every { bfs.calculate(any(), any(), any(), any(), any(), any(), any()) } returns PathResult.Success.Complete(
            tile
        )
        every { bfs.backtrace(any(), any(), 0, 0) } returns PathResult.Success.Complete(tile)
        // When
        bfs.find(tile, size, movement, strategy, traversal)
        // Then
        assertEquals(null, directions[1][2])
        assertEquals(99999999, distances[3][4])
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
        every { movement.directions } returns Array(GRAPH_SIZE) { arrayOfNulls<Direction?>(GRAPH_SIZE) }
        every { movement.distances } returns Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }
        every {
            bfs.calculate(
                any(),
                any(),
                tile.plane,
                size,
                movement,
                strategy,
                traversal
            )
        } returns PathResult.Failure
        every { bfs.calculatePartialPath(movement, strategy, any(), any()) } returns response
        // When
        val result = bfs.find(tile, size, movement, strategy, traversal)
        // Then
        verify { bfs.calculatePartialPath(movement, strategy, any(), any()) }
        assertEquals(response, result)
    }

    @Test
    fun `Finds route to target`() {
        // Given
        val size = Size(1, 1)
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }
        val directions = Array(GRAPH_SIZE) { arrayOfNulls<Direction?>(GRAPH_SIZE) }
        val queue = spyk(LinkedList<Tile>())
        every { movement.calc } returns queue
        every { movement.directions } returns directions
        every { movement.distances } returns distances
        every { strategy.reached(72, 74, 1, size) } returns true
        // When
        val result = bfs.calculate(10, 10, 1, size, movement, strategy, traversal)
        // Then
        verifyOrder {
            queue.add(Tile(64, 64))
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
        assertEquals(0, distances[64][64])
        assertEquals(Direction.NONE, directions[64][64])
    }

    @Test
    fun `Obstructions are ignored`() {
        // Given
        val size = Size(1, 1)
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }
        val directions = Array(GRAPH_SIZE) { arrayOfNulls<Direction?>(GRAPH_SIZE) }
        val queue = spyk(LinkedList<Tile>())
        every { movement.calc } returns queue
        every { movement.directions } returns directions
        every { movement.distances } returns distances
        every { strategy.reached(73, 74, 1, size) } returns true
        every { traversal.blocked(73, 74, 1, Direction.WEST) } returns true
        // When
        val result = bfs.calculate(10, 10, 1, size, movement, strategy, traversal)
        // Then
        verify(exactly = 0) {
            queue.add(Tile(73, 74, 1))
        }
        verifyOrder {
            queue.add(Tile(64, 64))
            queue.add(Tile(65, 64, 0))
        }
    }

    @Test
    fun `Partial calculation takes lowest cost`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) { 99999999 } }
        distances[5][5] = 2
        distances[4][5] = 3
        distances[3][5] = 4
        every { movement.distances } returns distances
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(movement, strategy, 10, 10)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(3, 5), result.last)
    }

    @Test
    fun `Partial calculation takes lowest distance`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) { 99999999 } }
        distances[5][5] = 2
        distances[6][6] = 2
        distances[7][7] = 2
        every { movement.distances } returns distances
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(movement, strategy, 10, 10)
        // Then
        assert(result is PathResult.Success.Partial)
        result as PathResult.Success.Partial
        assertEquals(Tile(5, 5), result.last)
    }

    @Test
    fun `Partial calculation returns failure if no values`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) { 99999999 } }
        every { movement.distances } returns distances
        every { strategy.tile } returns Tile(10, 10)
        // When
        val result = bfs.calculatePartialPath(movement, strategy, 10, 10)
        // Then
        assert(result is PathResult.Failure)
    }

    @Test
    fun `Backtrace steps`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val result = PathResult.Success.Complete(tile)
        val directions = Array(GRAPH_SIZE) { arrayOfNulls<Direction?>(GRAPH_SIZE) }
        directions[10][10] = Direction.NORTH
        directions[10][9] = Direction.EAST
        directions[9][9] = Direction.SOUTH
        directions[9][10] = Direction.SOUTH
        directions[9][11] = Direction.WEST
        directions[10][11] = Direction.NONE
        val steps: Steps = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        every { movement.directions } returns directions
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
        val directions = Array(GRAPH_SIZE) { arrayOfNulls<Direction?>(GRAPH_SIZE) }
        val steps: Steps = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        every { movement.directions } returns directions
        // When
        val result = bfs.backtrace(movement, PathResult.Success.Complete(tile), 0, 0)
        // Then
        assert(result is PathResult.Failure)
    }
}