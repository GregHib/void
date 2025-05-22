package content.entity.obj.ship

import WorldTest
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CharterShipTest : WorldTest() {
    @Test
    fun `Sail with charter ship`() {
        val player = createPlayer(Tile(3034, 3192))
        player.inventory.add("coins", 1000)
        val stan = createNPC("trader_stan", Tile(3033, 3192))

        player.npcOption(stan, "Charter")
        tick()
        player.interfaceOption("charter_ship_map", "catherby", "Ok")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1")
        tick(5)
        assertEquals(Tile(2792, 3417, 1), player.tile)
        assertEquals("port_sarim", player["charter_ship", ""])
    }

    @Test
    fun `Can't travel to same location`() {
        val player = createPlayer(Tile(2796, 3414))
        player.inventory.add("coins", 1000)
        val crew = createNPC("trader_crewmember_blue", Tile(3033, 3192))

        player.npcOption(crew, "Charter")
        tick()
        assertNull(player.dialogue)
    }
}