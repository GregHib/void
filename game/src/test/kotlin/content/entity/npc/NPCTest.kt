package content.entity.npc

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.world.script.WorldTest

internal class NPCTest : WorldTest() {

    @Test
    fun `Man randomly walks around`() {
        settings["world.npcs.randomWalk"] = "true"
        val spawn = emptyTile
        val npc = createNPC("chicken", spawn) { npc ->
            npc["area"] = Rectangle(spawn.minus(25, 25), 50, 50)
        }
        tickIf { npc.tile == spawn }

        assertNotEquals(spawn, npc.tile)
    }
}