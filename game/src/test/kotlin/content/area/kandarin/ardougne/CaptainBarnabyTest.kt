package content.area.kandarin.ardougne

import WorldTest
import containsMessage
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CaptainBarnabyTest : WorldTest() {

    @Test
    fun `Pay fare from Ardougne sails to Brimhaven`() {
        val player = createPlayer(Tile(2683, 3274))
        player.inventory.add("coins", 30)
        val barnaby = createNPC("captain_barnaby_2", Tile(2683, 3275))

        player.npcOption(barnaby, "Pay-fare")

        tickIf(limit = 200) { player.tile != Tile(2775, 3234, 1) }

        assertEquals(Tile(2775, 3234, 1), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Boarding the Ardougne ship warns to speak to Captain Barnaby`() {
        val player = createPlayer(Tile(2683, 3270))
        val gangplank = GameObjects.find(Tile(2683, 3270), "gangplank_ardougne_enter")

        player.objectOption(gangplank, "Cross")
        tickIf(limit = 20) { player.tile != Tile(2683, 3268, 1) }

        assertEquals(Tile(2683, 3268, 1), player.tile)
        assertTrue(player.containsMessage("You must speak to Captain Barnaby before it will set sail."))
    }

    @Test
    fun `Ship's ladder cannot be climbed`() {
        val player = createPlayer(Tile(2683, 3268, 1))
        val ladder = GameObjects.find(Tile(2682, 3267, 1), "captain_barnaby_ship_ladder")

        player.objectOption(ladder, "Climb-down")
        tick(2)

        assertTrue(player.containsMessage("I don't think Captain Barnaby wants me going down there."))
    }
}
