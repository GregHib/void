package world.gregs.voidps.engine.path

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.collision
import world.gregs.voidps.engine.path.algorithm.AxisAlignment
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.algorithm.DirectDiagonalSearch
import world.gregs.voidps.engine.path.algorithm.RetreatAlgorithm
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal

internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch
    lateinit var dd: DirectDiagonalSearch
    lateinit var retreat: RetreatAlgorithm
    lateinit var collisions: Collisions
    lateinit var provider: CollisionStrategyProvider
    lateinit var source: Character

    @BeforeEach
    fun setup() {
        source = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        dd = mockk(relaxed = true)
        retreat = mockk(relaxed = true)
        provider = mockk(relaxed = true)
        pf = spyk(PathFinder(aa, dd, bfs, retreat, provider))
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionStrategyKt")
    }

    @Test
    fun `Find tile`() {
        // Given
        val target = Tile(1, 1)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { source.size } returns Size.ONE
        every { source.collision } returns collision
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
        val target: Character = mockk(relaxed = true)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { source.collision } returns collision
        every { source.size } returns Size.ONE
        every { target.interactTarget } returns strategy
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
        pf.find(source, mockk<Path>(relaxed = true), PathType.Smart, false)
        // Then
        verify {
            bfs.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Player dumb finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Follow, false)
        // Then
        verify {
            dd.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `NPC finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Dumb, false)
        // Then
        verify {
            aa.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Retreat finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Retreat, false)
        // Then
        verify {
            retreat.find(any(), any(), any(), any(), any())
        }
    }

}