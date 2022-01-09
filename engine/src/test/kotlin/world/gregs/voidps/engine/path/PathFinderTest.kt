package world.gregs.voidps.engine.path

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.collision
import world.gregs.voidps.engine.path.algorithm.*
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal

internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var ds: DirectSearch
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch
    lateinit var dd: DirectDiagonalSearch
    lateinit var retreat: RetreatAlgorithm
    lateinit var collisions: Collisions
    lateinit var provider: CollisionStrategyProvider

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        ds = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        dd = mockk(relaxed = true)
        retreat = mockk(relaxed = true)
        provider = mockk(relaxed = true)
        pf = spyk(PathFinder(aa, ds, dd, bfs, retreat, provider))
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionStrategyKt")
    }

    @Test
    fun `Find tile`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target = Tile(1, 1)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { source.size } returns Size.ONE
        every { source.collision } returns collision
        every { pf.getAlgorithm(any()) } returns bfs
        every { provider.get(source, any()) } returns collision
        // When
        pf.find(source, target, PathType.Smart)
        // Then
        verify {
            bfs.find(source.tile, Size.ONE, any(), SmallTraversal, collision)
        }
    }

    @Test
    fun `Find entity`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target: Character = mockk(relaxed = true)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { source.collision } returns collision
        every { source.size } returns Size.ONE
        every { target.interactTarget } returns strategy
        every { pf.getAlgorithm(any()) } returns bfs
        every { provider.get(source, any()) } returns collision
        // When
        pf.find(source, target, PathType.Smart)
        // Then
        verify {
            bfs.find(source.tile, Size.ONE, any(), SmallTraversal, collision)
        }
    }

    @Test
    fun `Player smart finder`() {
        // When
        val finder = pf.getAlgorithm(PathType.Smart)
        // Then
        assertEquals(bfs, finder)
    }

    @Test
    fun `Player dumb finder`() {
        // When
        val finder = pf.getAlgorithm(PathType.Follow)
        // Then
        assertEquals(dd, finder)
    }

    @Test
    fun `NPC finder`() {
        // When
        val finder = pf.getAlgorithm(PathType.Dumb)
        // Then
        assertEquals(aa, finder)
    }

    @Test
    fun `Retreat finder`() {
        // When
        val finder = pf.getAlgorithm(PathType.Retreat)
        // Then
        assertEquals(retreat, finder)
    }

}