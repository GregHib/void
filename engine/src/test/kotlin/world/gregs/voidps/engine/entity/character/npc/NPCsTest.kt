package world.gregs.voidps.engine.entity.character.npc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.definition.types.NPCTypes
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile

class NPCsTest {

    private lateinit var npcs: NPCs

    @BeforeEach
    fun setup() {
        val definitions = NPCDefinitions(arrayOf(NPCDefinition(0)))
        NPCTypes.all = NPCTypes(1)
        NPCTypes.all.load(0, NPCDefinition(0, name = "test", stringId = "test"))
        npcs = NPCs(definitions, Collisions(emptyArray()), CollisionStrategyProvider(), AreaDefinitions())
    }

    @Test
    fun `Add character to list`() {
        val npc = npcs.add("test", Tile(1))
        assertEquals("test", npc.id)
        assertEquals(Tile(1), npc.tile)
        assertEquals(-1, npc.index)
        assertNull(npcs.indexed(1))
        npcs.run()

        assertEquals(1, npc.index)
        assertEquals(npc, npcs.indexed(1))
        assertEquals(1, npcs.size)
    }

    @Test
    fun `Invalid npcs aren't added`() {
        val npc = npcs.add("invalid", Tile(1))
        assertEquals("invalid", npc.id)
        assertEquals(Tile(1), npc.tile)
        npcs.run()
        assertEquals(-1, npc.index)
        assertNull(npcs.indexed(1))
        assertEquals(0, npcs.size)
    }

    @Test
    fun `Remove character from list`() {
        val npc = npcs.add("test", Tile(1))
        npcs.run()

        assertTrue(npcs.remove(npc))
        assertNotNull(npcs.indexed(1))

        npcs.run()

        assertNull(npcs.indexed(1))
        assertEquals(0, npcs.size)
    }

    @Test
    fun `Iterate over removed index`() {
        val npc1 = npcs.add("test", Tile(1))
        val npc2 = npcs.add("test", Tile(1))
        val npc3 = npcs.add("test", Tile(1))
        npcs.run()

        assertTrue(npcs.remove(npc2))
        npcs.run()

        val iterator = npcs.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(npc1, iterator.next())
        assertEquals(npc3, iterator.next())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `Clear all characters in list`() {
        npcs.add("test", Tile(1))
        npcs.run()
        npcs.clear()

        assertNull(npcs.indexed(1))
        assertEquals(0, npcs.size)
    }
}
