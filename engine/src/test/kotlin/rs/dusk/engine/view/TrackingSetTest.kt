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
    fun `Switch current and removal sets`() {
        // Given
        val current = set.current
        val remove = set.remove
        set.total = 1
        // When
        set.switch()
        // Then
        assertEquals(current, set.remove)
        assertEquals(remove, set.current)
        assertEquals(0, set.total)
    }

    @Test
    fun `Update previously unseen entity is added`() {
        // Given
        val npc = NPC(1, Tile(0))
        val entities = setOf(npc)
        // When
        set.update(entities)
        // Then
        assert(set.add.contains(npc))
    }

    @Test
    fun `Update seen entity is not changed`() {
        // Given
        val npc = NPC(1, Tile(0))
        set.remove.add(npc)
        val entities = setOf(npc)
        // When
        set.update(entities)
        // Then
        assert(set.current.contains(npc))
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Update exceeding maximum entities`() {
        // Given
        val npc = NPC(11, Tile(0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.update(entities)
        // Then
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Update within view`() {
        // Given
        val npc = NPC(1, Tile(15, 15, 0))
        val entities = setOf(npc)
        // When
        set.update(entities, 0, 0)
        // Then
        assert(set.add.contains(npc))
    }

    @Test
    fun `Update outside of view`() {
        // Given
        val npc = NPC(1, Tile(16, 16, 0))
        val entities = setOf(npc)
        // When
        set.update(entities, 0, 0)
        // Then
        assert(!set.add.contains(npc))
    }

    @Test
    fun `Update within exceeding maximum entities`() {
        // Given
        val npc = NPC(11, Tile(15, 15, 0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.update(entities, 0, 0)
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