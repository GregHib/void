package content.bot.path

import content.bot.interact.navigation.graph.Condition
import content.bot.interact.navigation.graph.Edge
import content.bot.interact.navigation.graph.NavigationGraph
import content.bot.interact.navigation.graph.waypoints
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.DijkstraFrontier
import content.bot.interact.path.EdgeTraversal
import content.bot.interact.path.NodeTargetStrategy
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import java.util.*
import kotlin.test.assertNotNull

internal class DijkstraTest {

    private lateinit var graph: NavigationGraph
    private lateinit var dij: Dijkstra
    private lateinit var pool: ObjectPool<DijkstraFrontier>

    @BeforeEach
    fun setup() {
        graph = NavigationGraph()
        pool = object : DefaultPool<DijkstraFrontier>(1) {
            override fun produceInstance() = DijkstraFrontier(3)
        }
        mockkStatic("content.bot.interact.navigation.graph.EdgeKt")
        dij = Dijkstra(graph, pool)
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

        val e1 = Edge("", player, a, 0)
        val e2 = Edge("", a, b, 0)
        val e3 = Edge("", b, c, 0)
        val e4 = Edge("", c, d, 0)
        graph.add(player, ObjectOpenHashSet.of(e1))
        graph.add(a, ObjectOpenHashSet.of(e2))
        graph.add(b, ObjectOpenHashSet.of(e3))
        graph.add(c, ObjectOpenHashSet.of(e4))

        val waypoints = LinkedList<Edge>()
        every { player.waypoints } returns waypoints

        val strategy: NodeTargetStrategy = object : NodeTargetStrategy() {
            override fun reached(node: Any): Boolean = node == c
        }
        val traversal = EdgeTraversal()
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assertNotNull(result)
        assertEquals(c, result)
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

        val edge = Edge("", player, b, 9)
        graph.add(player, ObjectOpenHashSet.of(Edge("", player, a, 10), edge))

        val waypoints = LinkedList<Edge>()
        every { player.waypoints } returns waypoints

        val strategy: NodeTargetStrategy = object : NodeTargetStrategy() {
            override fun reached(node: Any): Boolean = node != player
        }
        val traversal = EdgeTraversal()
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assertNotNull(result)
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

        val e1 = Edge("", player, a, 0)
        val e2 = Edge("", a, b, 0)
        val e3 = Edge("", b, c, 0)
        val e4 = Edge("", c, a, 0)
        graph.add(player, ObjectOpenHashSet.of(e1))
        graph.add(a, ObjectOpenHashSet.of(e2))
        graph.add(b, ObjectOpenHashSet.of(e3))
        graph.add(c, ObjectOpenHashSet.of(e4))

        val waypoints = LinkedList<Edge>()
        every { player.waypoints } returns waypoints

        var first = true
        val strategy: NodeTargetStrategy = object : NodeTargetStrategy() {
            override fun reached(node: Any): Boolean = if (node == a) {
                val answer = !first
                first = false
                answer
            } else {
                false
            }
        }
        val traversal = EdgeTraversal()
        // When
        val result = dij.find(player, strategy, traversal)
        // Then
        assertNotNull(result)
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
        val p: Player = mockk()
        val a = Tile(5, 10)

        val edge = Edge(
            "",
            p,
            a,
            9,
            requirements = listOf(
                object : Condition {
                    override fun has(player: Player): Boolean = player != p
                },
            ),
        )
        graph.add(p, ObjectOpenHashSet.of(edge))

        val waypoints = LinkedList<Edge>()
        every { p.waypoints } returns waypoints

        val strategy: NodeTargetStrategy = object : NodeTargetStrategy() {
            override fun reached(node: Any): Boolean = node == a
        }
        val traversal = EdgeTraversal()
        // When
        val result = dij.find(p, strategy, traversal)
        // Then
        assertNull(result)
        assertTrue(waypoints.isEmpty())
    }
}
