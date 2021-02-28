package world.gregs.voidps.engine.path.algorithm

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.io.pool.ObjectPool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import java.util.*

internal class DijkstraTest {


    private lateinit var graph: NavigationGraph
    private lateinit var dij: Dijkstra
    private lateinit var pool: ObjectPool<DijkstraFrontier>

    @BeforeEach
    fun setup() {
        graph = mockk(relaxed = true)
        pool = mockk(relaxed = true)
        every { pool.borrow() } returns DijkstraFrontier(3)
        dij = spyk(Dijkstra(graph, pool))
    }

    /**
     * P -- A -- B -- C -- D
     */
    @Test
    fun `Find path`() {
        val player: Player = mockk()
        val a = Tile(5, 10)
        val b = Tile(15, 0)
        val c = Tile(20, 0)
        val d = Tile(25, 0)

        val e1 = Edge(player, a, 0)
        val e2 = Edge(a, b, 0)
        val e3 = Edge(b, c, 0)
        val e4 = Edge(c, d, 0)
        every { graph.getAdjacent(player) } returns setOf(e1)
        every { graph.getAdjacent(a) } returns setOf(e2)
        every { graph.getAdjacent(b) } returns setOf(e3)
        every { graph.getAdjacent(c) } returns setOf(e4)

        val movement: Movement = mockk()
        val waypoints = LinkedList<Edge>()
        every { movement.waypoints } returns waypoints
        every { player.movement } returns movement

        val strategy: NodeTargetStrategy = mockk()
        every { strategy.reached(any()) } returns false
        every { strategy.reached(c) } returns true
        val traversal: EdgeTraversal = mockk()
        every { traversal.blocked(any(), any()) } returns false
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assert(result is PathResult.Success)
        result as PathResult.Success
        assertEquals(c, result.last)
        assertEquals(e1, waypoints.poll())
        assertEquals(e2, waypoints.poll())
        assertEquals(e3, waypoints.poll())
        assertTrue(waypoints.isEmpty())
    }

    /**       -- A
     *   10 /
     * P --
     *    9 \
     *       -- B
     */
    @Test
    fun `Find lowest cost path`() {
        val player: Player = mockk()
        val a = Tile(5, 10)
        val b = Tile(15, 0)

        val edge = Edge(player, b, 9)
        every { graph.getAdjacent(any()) } returns setOf()
        every { graph.getAdjacent(player) } returns setOf(Edge(player, a, 10), edge)

        val movement: Movement = mockk()
        val waypoints = LinkedList<Edge>()
        every { movement.waypoints } returns waypoints
        every { player.movement } returns movement

        val strategy: NodeTargetStrategy = mockk()
        every { strategy.reached(any()) } returns true
        every { strategy.reached(player) } returns false
        val traversal: EdgeTraversal = mockk()
        every { traversal.blocked(any(), any()) } returns false
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assert(result is PathResult.Success)
        result as PathResult.Success
        assertEquals(edge, waypoints.poll())
        assertTrue(waypoints.isEmpty())
    }

    /**      B
     *      / \
     * P - A < C
     *
     */
    @Test
    fun `Find directional twice visited node`() {
        val player: Player = mockk()
        val a = Tile(5, 10)
        val b = Tile(15, 0)
        val c = Tile(0, 0)

        val e1 = Edge(player, a, 0)
        val e2 = Edge(a, b, 0)
        val e3 = Edge(b, c, 0)
        val e4 = Edge(c, a, 0)
        every { graph.getAdjacent(player) } returns setOf(e1)
        every { graph.getAdjacent(a) } returns setOf(e2)
        every { graph.getAdjacent(b) } returns setOf(e3)
        every { graph.getAdjacent(c) } returns setOf(e4)

        val movement: Movement = mockk()
        val waypoints = LinkedList<Edge>()
        every { movement.waypoints } returns waypoints
        every { player.movement } returns movement

        val strategy: NodeTargetStrategy = mockk()
        every { strategy.reached(any()) } returns false
        var first = true
        every { strategy.reached(a) } answers {
            val answer = !first
            first = false
            answer
        }
        val traversal: EdgeTraversal = mockk()
        every { traversal.blocked(any(), any()) } returns false
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assert(result is PathResult.Success)
        result as PathResult.Success
        assertEquals(e1, waypoints.poll())
        assertEquals(e2, waypoints.poll())
        assertEquals(e3, waypoints.poll())
        assertEquals(e4, waypoints.poll())
        assertTrue(waypoints.isEmpty())
    }

    /**
     * P -/- A
     */
    @Test
    fun `Paths can be blocked`() {
        val player: Player = mockk()
        val a = Tile(5, 10)

        val edge = Edge(player, a, 9)
        every { graph.getAdjacent(player) } returns setOf(edge)

        val movement: Movement = mockk()
        val waypoints = LinkedList<Edge>()
        every { movement.waypoints } returns waypoints
        every { player.movement } returns movement

        val strategy: NodeTargetStrategy = mockk()
        every { strategy.reached(player) } returns false
        every { strategy.reached(a) } returns true
        val traversal: EdgeTraversal = mockk()
        every { traversal.blocked(player, edge) } returns true
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assert(result is PathResult.Failure)
        assertTrue(waypoints.isEmpty())
    }

}