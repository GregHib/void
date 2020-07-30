package rs.dusk.engine.view

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.character.npc.NPC
import rs.dusk.engine.model.entity.character.npc.NPCTrackingSet
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 24, 2020
 */
internal class NPCTrackingSetTest : KoinMock() {
    lateinit var set: NPCTrackingSet

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        set = NPCTrackingSet(
            tickMax = 4,
            maximum = 10,
            radius = 15
        )
    }

    @Test
    fun `Preparation fills removal set`() {
        // Given
        val npc = NPC(index = 1)
        set.current.add(npc)
        set.total = 1
        // When
        set.start(null)
        // Then
        assert(set.remove.contains(npc))
        assertEquals(0, set.total)
    }

    @Test
    fun `Tracking tracks self in total`() {
        // Given
        val client = NPC(index = 1)
        set.remove.add(client)
        // When
        set.track(setOf(client), client)
        // Then
        assertFalse(set.remove.contains(client))
        assertEquals(1, set.total)
        assertEquals(0, set.add.size)
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = NPC(index = 1)
        val toRemove = NPC(index = 2)
        val npc1 = NPC(index = 3)
        val npc2 = NPC(index = 4)
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
        assertFalse(set.current.contains(toRemove))
        assertEquals(3, set.total)
    }

    @Test
    fun `Previously unseen entity is added`() {
        // Given
        val npc = NPC(index = 1)
        val entities = setOf(npc)
        // When
        set.track(entities, null)
        // Then
        assertTrue(set.add.contains(npc))
    }

    @Test
    fun `Tracked and seen entity is not removed`() {
        // Given
        val npc = NPC(index = 1)
        set.remove.add(npc)
        val entities = setOf(npc)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.remove.contains(npc))
        assertFalse(set.add.contains(npc))
    }

    @Test
    fun `Track exceeding maximum entities`() {
        // Given
        val npc = NPC(index = 11, tile = Tile(0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.add.contains(npc))
    }

    @Test
    fun `Track within view`() {
        // Given
        val npc = NPC(index = 1, tile = Tile(15, 15, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertTrue(set.add.contains(npc))
    }

    @Test
    fun `Track outside of view`() {
        // Given
        val npc = NPC(index = 1, tile = Tile(16, 16, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc))
    }

    @Test
    fun `Track within exceeding maximum entities`() {
        // Given
        val npc = NPC(index = 11, tile = Tile(15, 15, 0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc))
    }

    @Test
    fun `Track within exceeding maximum tick entities`() {
        // Given
        val npc = NPC(index = 5, tile = Tile(15, 15, 0))
        set.add.addAll(setOf(mockk(), mockk(), mockk(), mockk()))
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc))
    }

    @Test
    fun `Visible but teleported entity is removed`() {
        // Given
        val npc = mockk<NPC>(relaxed = true)
        every { npc.index } returns 1
        every { npc.tile } returns Tile(15, 15, 0)
        every { npc.movement.delta } returns Tile(1)
        every { npc.movement.walkStep } returns Direction.NONE
        every { npc.movement.runStep } returns Direction.NONE
        set.remove.add(npc)
        // When
        set.track(npc, null)
        // Then
        assertTrue(set.remove.contains(npc))
        assertFalse(set.current.contains(npc))
    }

    @Test
    fun `Clear all entities`() {
        // Given
        set.add.add(NPC(index = 0, tile = Tile(0)))
        set.remove.add(NPC(index = 0, tile = Tile(0)))
        set.total = 2
        // When
        set.clear()
        // Then
        assert(set.add.isEmpty())
        assert(set.remove.isEmpty())
        assert(set.current.isEmpty())
        assertEquals(0, set.total)
    }

    @Test
    fun `Refresh all entities`() {
        // Given
        set.add.add(NPC(index = 1, tile = Tile(0)))
        set.current.add(NPC(index = 2, tile = Tile(0)))
        set.remove.add(NPC(index = 3, tile = Tile(0)))
        set.total = 2
        // When
        set.refresh()
        // Then
        assert(set.current.isEmpty())
        assertEquals(1, set.remove.size)
        assertEquals(2, set.add.size)
        assertEquals(0, set.total)
    }
}