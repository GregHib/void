package world.gregs.voidps.engine.path

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.strategy.IgnoredCollision
import world.gregs.voidps.engine.path.algorithm.AxisAlignment
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.algorithm.DirectDiagonalSearch
import world.gregs.voidps.engine.path.algorithm.RetreatAlgorithm

internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch
    lateinit var dd: DirectDiagonalSearch
    lateinit var retreat: RetreatAlgorithm
    lateinit var collisions: Collisions
    lateinit var ignored: IgnoredCollision
    lateinit var source: Character

    @BeforeEach
    fun setup() {
        source = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        dd = mockk(relaxed = true)
        retreat = mockk(relaxed = true)
        ignored = mockk(relaxed = true)
        pf = spyk(PathFinder(aa, dd, bfs, retreat, ignored))
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionStrategyKt")
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