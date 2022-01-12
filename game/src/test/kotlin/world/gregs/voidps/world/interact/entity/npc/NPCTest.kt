package world.gregs.voidps.world.interact.entity.npc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.world.script.WorldMock

internal class NPCTest : WorldMock() {

    @Test
    fun `Man randomly walks around`() = runBlocking(Dispatchers.Default) {
        val spawn = Tile(100, 100)
        val npc = createNPC("hans", spawn) { npc ->
            npc["area"] = Rectangle(spawn.minus(25, 25), 50, 50)
            npc.start("no_clip")
        }
        tickIf { npc.tile == spawn }

        assertNotEquals(spawn, npc.tile)
    }

}