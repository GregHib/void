package world.gregs.voidps.engine.path

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.algorithm.AxisAlignment
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.algorithm.DirectDiagonalSearch
import world.gregs.voidps.engine.path.algorithm.DirectSearch
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal

internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var ds: DirectSearch
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch
    lateinit var dd: DirectDiagonalSearch
    lateinit var collisions: Collisions

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        ds = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        dd = mockk(relaxed = true)
        pf = spyk(PathFinder(collisions, aa, ds, dd, bfs))
    }

    @Test
    fun `Find tile`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target = Tile(1, 1)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { pf.getAlgorithm(any(), any()) } returns bfs
        // When
        pf.find(source, target)
        // Then
        verify {
            bfs.find(source.tile, source.size, any(), SmallTraversal, collision)
        }
    }

    @Test
    fun `Find entity`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target: Character = mockk(relaxed = true)
        val strategy: TileTargetStrategy = mockk(relaxed = true)
        val collision: CollisionStrategy = mockk(relaxed = true)
        every { target.interactTarget } returns strategy
        every { pf.getAlgorithm(any(), any()) } returns bfs
        // When
        pf.find(source, target)
        // Then
        verify {
            bfs.find(source.tile, source.size, any(), SmallTraversal, collision)
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