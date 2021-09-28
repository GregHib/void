package world.gregs.voidps.world.interact.entity.npc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.world.script.WorldMock

internal class NPCTest : WorldMock() {

    @Test
    fun `Man randomly walks around`() = runBlocking(Dispatchers.Default) {
        val spawn = Tile(100, 100)
        val npc = createNPC("hans", spawn)
        npc["area"] = Rectangle(spawn, 50, 50)

        tickIf { npc.tile == spawn }

        assertNotEquals(spawn, npc.tile)
    }

}