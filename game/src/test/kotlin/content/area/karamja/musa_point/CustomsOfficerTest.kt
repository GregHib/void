package content.area.karamja.musa_point

import WorldTest
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class CustomsOfficerTest : WorldTest() {

    @Test
    fun `Pay fare from Brimhaven sails to Ardougne`() {
        val player = createPlayer(Tile(2772, 3226))
        player.inventory.add("coins", 30)
        val officer = createNPC("customs_officer_brimhaven", Tile(2772, 3225))

        player.npcOption(officer, "Pay-Fare")

        tickIf(limit = 200) { player.tile != Tile(2683, 3268, 1) }

        assertEquals(Tile(2683, 3268, 1), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Pay fare from Musa Point sails to Port Sarim`() {
        val player = createPlayer(Tile(2953, 3148))
        player.inventory.add("coins", 30)
        val officer = createNPC("customs_officer_brimhaven", Tile(2953, 3147))

        player.npcOption(officer, "Pay-Fare")

        tickIf(limit = 200) { player.tile != Tile(3032, 3217, 1) }

        assertEquals(Tile(3032, 3217, 1), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Cross gangplank to disembark at Ardougne docks`() {
        val player = createPlayer(Tile(2683, 3268, 1))
        val gangplank = GameObjects.find(Tile(2683, 3269, 1), "gangplank_ardougne_exit")

        player.objectOption(gangplank, "Cross")
        tickIf(limit = 20) { player.tile != Tile(2683, 3271) }

        assertEquals(Tile(2683, 3271), player.tile)
    }

    @Test
    fun `Cross gangplank to board ship at Brimhaven docks`() {
        val player = createPlayer(Tile(2772, 3234))
        val gangplank = GameObjects.find(Tile(2773, 3234), "gangplank_brimhaven_enter")

        player.objectOption(gangplank, "Cross")
        tickIf(limit = 20) { player.tile != Tile(2775, 3234, 1) }

        assertEquals(Tile(2775, 3234, 1), player.tile)
    }
}
