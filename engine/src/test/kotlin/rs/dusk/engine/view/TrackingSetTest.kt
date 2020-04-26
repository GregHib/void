package rs.dusk.engine.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 24, 2020
 */
internal class TrackingSetTest {
    lateinit var set: TrackingSet<NPC>

    @BeforeEach
    fun setup() {
        set = TrackingSet(maximum = 10, radius = 15)
    }

    @Test
    fun `Prep removal set`() {
        // Given
        val npc = NPC(1, Tile(0))
        set.current.add(npc)
        set.total = 1
        // When
        set.prep()
        // Then
        assert(set.remove.contains(npc))
        assertEquals(0, set.total)
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = NPC(1, Tile(0))
        val toRemove = NPC(2, Tile(0))
        val npc1 = NPC(3, Tile(0))
        val npc2 = NPC(4, Tile(0))
        set.current.addAll(listOf(npc1, toRemove, npc2))
        set.remove.add(toRemove)
        set.add.add(toAdd)
        set.total = 3
        // When
        set.update()
        // Then
        assert(set.add.isEmpty())
        assert(set.remove.isEmpty())
        assert(set.current.contains(toAdd))
        assert(!set.current.contains(toRemove))
        assertEquals(3, set.total)
    }

    @Test
    fun `Previously unseen entity is added`() {
        // Given
        val npc = NPC(1, Tile(0))
        val entities = setOf(npc)
        // When
        set.track(entities)
        // Then
        assert(set.add.contains(npc))
    }

    @Test
    fun `Tracked and seen entity is not removed`() {
        // Given
        val npc = NPC(1, Tile(0))
        set.remove.add(npc)
        val entities = setOf(npc)
        // When
        set.track(entities)
        // Then
        assert(!set.remove.contains(npc))
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Track exceeding maximum entities`() {
        // Given
        val npc = NPC(11, Tile(0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.track(entities)
        // Then
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Track within view`() {
        // Given
        val npc = NPC(1, Tile(15, 15, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, 0, 0)
        // Then
        assert(set.add.contains(npc))
    }

    @Test
    fun `Track outside of view`() {
        // Given
        val npc = NPC(1, Tile(16, 16, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, 0, 0)
        // Then
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Track within exceeding maximum entities`() {
        // Given
        val npc = NPC(11, Tile(15, 15, 0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.track(entities, 0, 0)
        // Then
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Clear all entities`() {
        // Given
        set.add.add(NPC(0, Tile(0)))
        set.remove.add(NPC(0, Tile(0)))
        set.total = 2
        // When
        set.clear()
        // Then
        assert(set.add.isEmpty())
        assert(set.remove.isEmpty())
        assert(set.current.isEmpty())
        assertEquals(0, set.total)
    }
}