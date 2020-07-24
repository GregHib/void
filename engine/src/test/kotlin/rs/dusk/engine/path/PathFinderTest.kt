package rs.dusk.engine.path

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.obj.GameObject
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.path.find.AxisAlignment
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.path.find.DirectSearch
import rs.dusk.engine.path.target.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 21, 2020
 */
internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var collisions: Collisions
    lateinit var ds: DirectSearch
    lateinit var aa: AxisAlignment
    lateinit var bfs: BreadthFirstSearch

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        ds = mockk(relaxed = true)
        aa = mockk(relaxed = true)
        bfs = mockk(relaxed = true)
        pf = spyk(PathFinder(collisions, aa, ds, bfs))
    }

    @Test
    fun `Find tile`() {
        // Given
        val source: Character = mockk(relaxed = true)
        val target = Tile(1, 1)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        every { source.movement.traversal } returns traversal
        every { pf.getFinder(any()) } returns bfs
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
        val target: Entity = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val strategy: TargetStrategy = mockk(relaxed = true)
        every { source.movement.traversal } returns traversal
        every { pf.getStrategy(any()) } returns strategy
        every { pf.getFinder(any()) } returns bfs
        // When
        pf.find(source, target)
        // Then
        verify {
            bfs.find(source.tile, source.size, source.movement, strategy, traversal)
        }
    }

    @Test
    fun `Player finder`() {
        // Given
        val source: Player = mockk(relaxed = true)
        // When
        val finder = pf.getFinder(source)
        // Then
        assertEquals(bfs, finder)
    }

    @Test
    fun `NPC finder`() {
        // Given
        val source: NPC = mockk(relaxed = true)
        // When
        val finder = pf.getFinder(source)
        // Then
        assertEquals(aa, finder)
    }

    @Test
    fun `Floor item strategy`() {
        // Given
        val target: FloorItem = mockk(relaxed = true)
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(PointTargetStrategy(target.tile, target.size), strategy)
    }

    @Test
    fun `NPC strategy`() {
        // Given
        val target: NPC = mockk(relaxed = true)
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(RectangleTargetStrategy(collisions, target.tile, target.size), strategy)
    }

    @Test
    fun `Player strategy`() {
        // Given
        val target: Player = mockk(relaxed = true)
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(RectangleTargetStrategy(collisions, target.tile, target.size), strategy)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 9])
    fun `Wall object strategy`(type: Int) {
        // Given
        val target: GameObject = mockk(relaxed = true)
        every { target.type } returns type
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(WallTargetStrategy(collisions, target.tile, target.size, target.rotation, target.type), strategy)
    }

    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8])
    fun `Wall decoration object strategy`(type: Int) {
        // Given
        val target: GameObject = mockk(relaxed = true)
        every { target.type } returns type
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(
            DecorationTargetStrategy(collisions, target.tile, target.size, target.rotation, target.type),
            strategy
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 11, 22])
    fun `Floor decoration object strategy`(type: Int) {
        // Given
        val target: GameObject = mockk(relaxed = true)
        every { target.type } returns type
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(RectangleTargetStrategy(collisions, target.tile, target.size, target.def.blockFlag), strategy)
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24])
    fun `Other object strategy`(type: Int) {
        // Given
        val target: GameObject = mockk(relaxed = true)
        every { target.type } returns type
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(TileTargetStrategy(target.tile), strategy)
    }

    @Test
    fun `Other strategy`() {
        // Given
        val target: Entity = mockk(relaxed = true)
        // When
        val strategy = pf.getStrategy(target)
        // Then
        assertEquals(TileTargetStrategy(target.tile), strategy)
    }

}