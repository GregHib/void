package world.gregs.voidps.tools.graph

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Disabled
internal class MapGraphTest {

    private lateinit var strategy: TileTraversalStrategy
    private lateinit var collisions: Collisions
    private lateinit var graph: MapGraph

    @BeforeEach
    fun setup() {
        strategy = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        graph = MapGraph(mockk(), mockk(), mockk(), collisions)
    }

    @Test
    fun `Flood fill empty tile`() {
        val tile = Tile(0, 0)
        val tiles = graph.getFloodedTiles(strategy, tile, tile.toCuboid(width = 1, height = 1))
        assertEquals(mapOf(tile to 0), tiles)
    }

    @Test
    fun `Flood fill 2x2`() {
        val tile = Tile(0, 0)
        val tiles = graph.getFloodedTiles(strategy, tile, tile.toCuboid(width = 2, height = 2))
        assertEquals(mapOf(tile to 0, Tile(0, 1) to 1, Tile(1, 0) to 1, Tile(1, 1) to 1), tiles)
    }

    @Test
    fun `Flood fill 2x2 offset start`() {
        val tile = Tile(1, 1)
        val tiles = graph.getFloodedTiles(strategy, tile, Tile(0).toCuboid(width = 2, height = 2))
        assertEquals(mapOf(tile to 0, Tile(1, 0) to 1, Tile(0, 1) to 1, Tile(0, 0) to 1), tiles)
    }

    @Test
    fun `Flood fill 2x2 ignore collision`() {
        every { strategy.blocked(collisions, Tile(0, 0), 1, Direction.NORTH_EAST) } returns true
        every { strategy.blocked(collisions, Tile(0, 1), 1, Direction.EAST) } returns true
        every { strategy.blocked(collisions, Tile(1, 0), 1, Direction.NORTH) } returns true
        val tile = Tile(0, 0)
        val tiles = graph.getFloodedTiles(strategy, tile, tile.toCuboid(width = 2, height = 2))
        assertEquals(mapOf(tile to 0, Tile(0, 1) to 1, Tile(1, 0) to 1), tiles)
    }

    @Test
    fun `Other levels collision ignored`() {
        every { strategy.blocked(collisions, Tile(0, 0, 1), 1, any()) } returns true
        val tile = Tile(0, 0)
        val tiles = graph.getFloodedTiles(strategy, tile, tile.toCuboid(width = 1, height = 1))
        assertEquals(mapOf(tile to 0), tiles)
    }

    @Test
    fun `Euclidean distance`() {
        assertEquals(5.0, graph.euclidean(Tile(3, 4), Tile(7, 1)))
    }

    @Test
    fun `Find centroid of a line`() {
        assertEquals(Tile(0, 1), graph.centroid(setOf(Tile(0, 0), Tile(0, 2))))
    }

    @Test
    fun `Find centroid of a diagonal line`() {
        assertEquals(Tile(1, 1), graph.centroid(setOf(Tile(0, 0), Tile(2, 2))))
    }

    @Test
    fun `Find centroid of a square`() {
        assertEquals(Tile(1, 1), graph.centroid(setOf(Tile(0, 0), Tile(0, 2), Tile(2, 2), Tile(2, 0))))
    }

    @Test
    fun `Find centroid of a rectangle`() {
        assertEquals(Tile(1, 1), graph.centroid(setOf(Tile(0, 0), Tile(0, 2), Tile(3, 2), Tile(3, 0))))
    }

    @Test
    fun `Find filled points`() {
        assertEquals(listOf(Tile(0, 0)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 2, height = 2)))
    }

    @Test
    fun `No free center points`() {
        every { strategy.blocked(collisions, any(), 1, any()) } returns true
        assertEquals(listOf<Tile>(), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 3, height = 3)))
    }

    @Test
    fun `Find not collided points`() {
        every { strategy.blocked(collisions, Tile(0, 0), 1, Direction.NONE) } returns true
        every { strategy.blocked(collisions, Tile(1, 0), 1, Direction.WEST) } returns true
        assertEquals(listOf(Tile(1, 0)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 3, height = 2)))
    }

    @Test
    fun `Find two separated filled points`() {
        every { strategy.blocked(collisions, Tile(0, 1), 1, Direction.NONE) } returns true
        every { strategy.blocked(collisions, Tile(0, 0), 1, Direction.NORTH) } returns true
        every { strategy.blocked(collisions, Tile(0, 2), 1, Direction.SOUTH) } returns true
        assertEquals(listOf(Tile(0, 0), Tile(0, 2)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 2, height = 4)))
    }

    @Test
    fun `Find one point for two connected tiles`() {
        assertEquals(listOf(Tile(0, 0)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 2, height = 2)))
    }

    @Test
    fun `Find two points for two separated knots of three tiles each`() {
        for (x in 0 until 3) {
            every { strategy.blocked(collisions, Tile(x, 1), 1, Direction.NONE) } returns true
            every { strategy.blocked(collisions, Tile(x, 0), 1, Direction.NORTH) } returns true
            every { strategy.blocked(collisions, Tile(x, 0), 1, Direction.NORTH_EAST) } returns true
            every { strategy.blocked(collisions, Tile(x, 0), 1, Direction.NORTH_WEST) } returns true
            every { strategy.blocked(collisions, Tile(x, 2), 1, Direction.SOUTH) } returns true
            every { strategy.blocked(collisions, Tile(x, 2), 1, Direction.SOUTH_EAST) } returns true
            every { strategy.blocked(collisions, Tile(x, 2), 1, Direction.SOUTH_WEST) } returns true
        }
        assertEquals(listOf(Tile(1, 0), Tile(1, 2)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 3, height = 3)))
    }

    @Test
    fun `Find closest free tile to center point`() {
        val center = Tile(1, 1)
        every { strategy.blocked(collisions, center, 1, Direction.NONE) } returns true
        for (dir in Direction.all) {
            every { strategy.blocked(collisions, center.minus(dir.delta), 1, dir) } returns true
        }
        assertEquals(listOf(Tile(0, 1)), graph.getCenterPoints(strategy, Tile(0).toCuboid(width = 3, height = 3)))
    }

    @Test
    fun `Link between two points`() {
        val points = setOf(Tile(0, 0), Tile(1, 0))
        val results = graph.getStaticLinks(strategy, points, 2)
        assertEquals(setOf(Triple(Tile(0, 0), Tile(1, 0), 1), Triple(Tile(1, 0), Tile(0, 0), 1)), results)
    }

    @Test
    fun `Don't link points outside of area`() {
        val points = setOf(Tile(0, 0), Tile(6, 0))
        val results = graph.getStaticLinks(strategy, points, 2)
        assertEquals(setOf<Triple<Tile, Tile, Int>>(), results)
    }

    @Test
    fun `Link between multiple points`() {
        val points = setOf(Tile(0, 0), Tile(3, 3), Tile(1, 1))
        val results = graph.getStaticLinks(strategy, points, 4)
        assertEquals(
            setOf(
                Triple(Tile(0, 0), Tile(1, 1), 1),
                Triple(Tile(0, 0), Tile(3, 3), 3),
                Triple(Tile(3, 3), Tile(1, 1), 2),
                Triple(Tile(3, 3), Tile(0, 0), 3),
                Triple(Tile(1, 1), Tile(0, 0), 1),
                Triple(Tile(1, 1), Tile(3, 3), 2),
            ),
            results,
        )
    }

    @Test
    fun `Get unlinked point`() {
        val points = setOf(Tile(0, 0))
        val links = setOf(Triple(Tile(1, 1), Tile(2, 2), 1))
        val results = graph.getUnlinkedPoints(points, links)
        assertEquals(points, results)
    }

    @Test
    fun `Unidirectional point isn't unlinked`() {
        val points = setOf(Tile(0, 0))
        val links = setOf(Triple(Tile(0, 0), Tile(1, 1), 1))
        val results = graph.getUnlinkedPoints(points, links)
        assertEquals(setOf<Tile>(), results)
    }

    @Test
    fun `Unidirectional inverse point isn't unlinked`() {
        val points = setOf(Tile(0, 0))
        val links = setOf(Triple(Tile(1, 1), Tile(0, 0), 1))
        val results = graph.getUnlinkedPoints(points, links)
        assertEquals(setOf<Tile>(), results)
    }

    @Test
    fun `Identify portals`() {
        val objects = mutableSetOf<GameObject>()
        val results = graph.getPortals(objects)
        assertEquals(setOf<Tile>(), results)
    }

    @Test
    fun `Get duplicate paths`() {
        val links = setOf(
            Triple(Tile(0, 0), Tile(1, 0), 1),
            Triple(Tile(1, 0), Tile(0, 1), 1),
            Triple(Tile(0, 1), Tile(0, 0), 1),
        )
        val results = graph.getDuplicatePaths(links)
        assertEquals(setOf(Triple(Tile(0, 0), Tile(1, 0), 1)), results)
    }
}
