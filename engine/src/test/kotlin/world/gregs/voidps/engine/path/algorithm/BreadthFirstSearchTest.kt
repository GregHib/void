package world.gregs.voidps.engine.path.algorithm

import io.mockk.*
import kotlinx.io.pool.ObjectPool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Steps
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import world.gregs.voidps.engine.value

/**
 * @author GregHib <greg@gregs.world>
 * @since May 20, 2020
 */
internal class BreadthFirstSearchTest {

    private lateinit var bfs: BreadthFirstSearch
    private lateinit var pool: ObjectPool<BreadthFirstSearchFrontier>

    @BeforeEach
    fun setup() {
        pool = mockk(relaxed = true)
        every { pool.borrow() } returns BreadthFirstSearchFrontier()
        bfs = spyk(BreadthFirstSearch(pool))
    }

    @Test
    fun `Partial path calculated if no complete path found`() {
        // Given
        val tile = Tile(64, 64)
        val size = Size(1, 1)
        val movement: Movement = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val response = PathResult.Success(Tile(64, 64))
        every { bfs.calculate(any(), size, strategy, traversal) } returns PathResult.Failure
        every { bfs.calculatePartialPath(any(), tile, strategy) } returns response
        // When
        bfs.find(tile, size, movement, strategy, traversal)
        // Then
        verify { bfs.calculatePartialPath(any(), tile, strategy) }
    }

    @Test
    fun `Finds route to target`() {
        // Given
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val discovery = BreadthFirstSearchFrontier()
        discovery.start(Tile(74, 74, 1))
        every { strategy.reached(72, 74, 1, size) } returns true
        // When
        bfs.calculate(discovery, size, strategy, traversal)
        // Then
        verifyOrder {
            strategy.reached(Tile(74, 74, 1), size)
            strategy.reached(Tile(73, 74, 1), size)// West
            strategy.reached(Tile(75, 74, 1), size)// East
            strategy.reached(Tile(74, 73, 1), size)// South
            strategy.reached(Tile(74, 75, 1), size)// North
            strategy.reached(Tile(73, 73, 1), size)// South west
            strategy.reached(Tile(75, 73, 1), size)// South east
            strategy.reached(Tile(73, 75, 1), size)// North west
            strategy.reached(Tile(75, 75, 1), size)// North east
        }
    }

    @Test
    fun `Obstructions are ignored`() {
        // Given
        val size = Size(1, 1)
        val strategy: TargetStrategy = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val discovery = BreadthFirstSearchFrontier()
        discovery.start(Tile(74, 74, 1))
        every { strategy.reached(Tile(73, 74, 1), size) } returns true
        every { traversal.blocked(Tile(73, 74, 1), Direction.WEST) } returns true
        // When
        bfs.calculate(discovery, size, strategy, traversal)
        // Then
        verify { strategy.reached(Tile(73, 74, 1), size) }
        verify(exactly = 0) { strategy.reached(Tile(75, 74), size) }
    }

    @Test
    fun `Partial calculation takes lowest cost`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        val frontier: BreadthFirstSearchFrontier = mockk(relaxed = true)
        every { frontier.mapSize } returns 128
        every { frontier.visited(any<Int>(), any()) } returns false
        every { frontier.visited(69, 69) } returns true
        every { frontier.visited(68, 69) } returns true
        every { frontier.visited(67, 69) } returns true
        every { frontier.cost(69, 69) } returns 2
        every { frontier.cost(68, 69) } returns 3
        every { frontier.cost(67, 69) } returns 4
        every { strategy.tile } returns value(Tile(64, 64))
        // When
        val result = bfs.calculatePartialPath(frontier, Tile(64, 64), strategy)
        // Then
        assert(result is PathResult.Partial)
        result as PathResult.Partial
        assertEquals(Tile(67, 69), result.last)
    }

    @Test
    fun `Partial calculation takes lowest distance`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        val frontier: BreadthFirstSearchFrontier = mockk(relaxed = true)
        every { frontier.mapSize } returns 128
        every { frontier.visited(any<Int>(), any()) } returns false
        every { frontier.visited(69, 69) } returns true
        every { frontier.visited(68, 69) } returns true
        every { frontier.visited(67, 69) } returns true
        every { frontier.cost(69, 69) } returns 2
        every { frontier.cost(68, 69) } returns 2
        every { frontier.cost(67, 69) } returns 2
        every { strategy.tile } returns value(Tile(64, 64))
        // When
        val result = bfs.calculatePartialPath(frontier, Tile(64, 64), strategy)
        // Then
        assert(result is PathResult.Partial)
        result as PathResult.Partial
        assertEquals(Tile(67, 69), result.last)
    }

    @Test
    fun `Partial calculation returns failure if no values`() {
        // Given
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { strategy.tile } returns value(Tile(74, 74))
        val frontier: BreadthFirstSearchFrontier = mockk(relaxed = true)
        every { frontier.mapSize } returns 128
        // When
        val result = bfs.calculatePartialPath(frontier, Tile(74, 74), strategy)
        // Then
        assert(result is PathResult.Failure)
    }

    @Test
    fun `Backtrace steps`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val tile = Tile(74, 74)
        val result = PathResult.Success(tile)
        val frontier: BreadthFirstSearchFrontier = mockk(relaxed = true)
        every { frontier.mapSize } returns 128
        every { frontier.cost(69, 69) } returns 2
        every { frontier.visited(anyValue<Tile>(), any()) } returns false
        every { frontier.visited(Tile(74, 74), any()) } returns true
        every { frontier.visited(Tile(74, 73), any()) } returns true
        every { frontier.visited(Tile(73, 73), any()) } returns true
        every { frontier.visited(Tile(73, 74), any()) } returns true
        every { frontier.visited(Tile(73, 75), any()) } returns true
        every { frontier.visited(Tile(74, 75), any()) } returns true
        every { frontier.direction(anyValue()) } returns Direction.NONE
        every { frontier.direction(Tile(74, 74)) } returns Direction.NORTH
        every { frontier.direction(Tile(74, 73)) } returns Direction.EAST
        every { frontier.direction(Tile(73, 73)) } returns Direction.SOUTH
        every { frontier.direction(Tile(73, 74)) } returns Direction.SOUTH
        every { frontier.direction(Tile(73, 75)) } returns Direction.WEST
        val steps: Steps = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        // When
        bfs.backtrace(movement, frontier, result, Tile(74, 74), Tile(74, 75))
        // Then
        verifyOrder {
            steps.addFirst(Direction.NORTH)
            steps.addFirst(Direction.EAST)
            steps.addFirst(Direction.SOUTH)
            steps.addFirst(Direction.SOUTH)
            steps.addFirst(Direction.WEST)
        }
    }

    @Test
    fun `Backtrace returns result even if no movement`() {
        // Given
        val movement: Movement = mockk(relaxed = true)
        val tile = Tile(10, 10)
        val steps: Steps = mockk(relaxed = true)
        val frontier: BreadthFirstSearchFrontier = mockk(relaxed = true)
        every { movement.steps } returns steps
        every { steps.count() } returns 1
        // When
        val result = bfs.backtrace(movement, frontier, PathResult.Success(tile), tile, tile)
        // Then
        assert(result is PathResult.Success)
    }
}