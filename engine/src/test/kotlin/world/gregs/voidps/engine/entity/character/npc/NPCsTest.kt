package world.gregs.voidps.engine.entity.character.npc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.type.Tile

class NPCsTest {

    @BeforeEach
    fun setup() {
        val definitions = NPCDefinitions.init(arrayOf(NPCDefinition(0)))
        definitions.ids = mapOf("test" to 0)
        NPCs.clear()
    }

    @Test
    fun `Add character to list`() {
        val npc = NPCs.add("test", Tile(1))
        assertEquals("test", npc.id)
        assertEquals(Tile(1), npc.tile)
        assertEquals(-1, npc.index)
        assertNull(NPCs.indexed(1))
        NPCs.run()

        assertEquals(1, npc.index)
        assertEquals(npc, NPCs.indexed(1))
        assertEquals(1, NPCs.size)
    }

    @Test
    fun `Invalid NPCs aren't added`() {
        val npc = NPCs.add("invalid", Tile(1))
        assertEquals("invalid", npc.id)
        assertEquals(Tile(1), npc.tile)
        NPCs.run()
        assertEquals(-1, npc.index)
        assertNull(NPCs.indexed(1))
        assertEquals(0, NPCs.size)
    }

    @Test
    fun `Remove character from list`() {
        val npc = NPCs.add("test", Tile(1))
        NPCs.run()

        assertTrue(NPCs.remove(npc))
        assertNotNull(NPCs.indexed(1))

        NPCs.run()

        assertNull(NPCs.indexed(1))
        assertEquals(0, NPCs.size)
    }

    @Test
    fun `Iterate over removed index`() {
        val npc1 = NPCs.add("test", Tile(1))
        val npc2 = NPCs.add("test", Tile(1))
        val npc3 = NPCs.add("test", Tile(1))
        NPCs.run()

        assertTrue(NPCs.remove(npc2))
        NPCs.run()

        val iterator = NPCs.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(npc1, iterator.next())
        assertEquals(npc3, iterator.next())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `Clear all characters in list`() {
        NPCs.add("test", Tile(1))
        NPCs.run()
        NPCs.clear()

        assertNull(NPCs.indexed(1))
        assertEquals(0, NPCs.size)
    }
}
