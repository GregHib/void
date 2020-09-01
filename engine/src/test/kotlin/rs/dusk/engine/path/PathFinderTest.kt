package rs.dusk.engine.path

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.algorithm.AxisAlignment
import rs.dusk.engine.path.algorithm.BreadthFirstSearch
import rs.dusk.engine.path.algorithm.DirectDiagonalSearch
import rs.dusk.engine.path.algorithm.DirectSearch
import rs.dusk.engine.path.strat.TileTargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 21, 2020
 */
internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var ds: DirectSearch
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch
    lateinit var dd: DirectDiagonalSearch

    @BeforeEach
    fun setup() {
        ds = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        dd = mockk(relaxed = true)
        pf = spyk(PathFinder(aa, ds, dd, bfs))
    }

    @Test
    fun `Find tile`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target = Tile(1, 1)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { source.movement.traversal } returns traversal
        every { pf.getAlgorithm(any(), any()) } returns bfs
        // When
        pf.find(source, target)
        // Then
        verify {
            bfs.find(source.tile, source.size, source.movement, any<TileTargetStrategy>(), traversal)
        }
    }

    @Test
    fun `Find entity`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target: Character = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { source.movement.traversal } returns traversal
        every { target.interactTarget } returns strategy
        every { pf.getAlgorithm(any(), any()) } returns bfs
        // When
        pf.find(source, target)
        // Then
        verify {
            bfs.find(source.tile, source.size, source.movement, strategy, traversal)
        }
    }

    @Test
    fun `Player smart finder`() {
        // Given
        val source: Player = mockk(relaxed = true)
        // When
        val finder = pf.getAlgorithm(source, true)
        // Then
        assertEquals(bfs, finder)
    }

    @Test
    fun `Player dumb finder`() {
        // Given
        val source: Player = mockk(relaxed = true)
        // When
        val finder = pf.getAlgorithm(source, false)
        // Then
        assertEquals(dd, finder)
    }

    @Test
    fun `NPC finder`() {
        // Given
        val source: NPC = mockk(relaxed = true)
        // When
        val finder = pf.getAlgorithm(source, true)
        // Then
        assertEquals(aa, finder)
    }

}