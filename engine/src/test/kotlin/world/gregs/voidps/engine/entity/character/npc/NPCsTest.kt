package world.gregs.voidps.engine.entity.character.npc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile

class NPCsTest {

    @Test
    fun `Iterate through list`() {
        val definitions = NPCDefinitions(arrayOf(NPCDefinition(0)))
        definitions.ids = mapOf("test" to 0)
        val npcs = NPCs(definitions, Collisions(emptyArray()), CollisionStrategyProvider(), AreaDefinitions())
        val npc1 = npcs.add("test", Tile(123, 456))
        val npc2 = npcs.add("test", Tile(123, 456))
        npcs.run()
        val npc3 = npcs.add("test", Tile(123, 456))
        assertEquals(1, npc1.index)
        assertEquals(2, npc2.index)
        assertEquals(-1, npc3.index)
        npcs.run()


        val npc4 = npcs.add("test", Tile(123, 456))
        npcs.run()
        assertEquals(2, npc4.index)
    }
}