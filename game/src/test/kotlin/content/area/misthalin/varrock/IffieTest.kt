package content.area.misthalin.varrock

import WorldTest
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IffieTest : WorldTest() {

    private fun visit(name: String, points: Int): Player {
        val player = createPlayer(Tile(3204, 3418), name)
        if (points > 0) {
            player["costume_points"] = points
        }
        val iffie = createNPC("iffie", Tile(3204, 3419))
        player.npcOption(iffie, "Claim-costume")
        tick(3)
        return player
    }

    @Test
    fun `The store shows the player's points on every costume row`() {
        val player = visit("iffie_stock", points = 3)

        assertTrue(player.interfaces.contains("costume_reward_select"))
        assertEquals(3, player.get("costume_points_mime", 0))
        assertEquals(3, player.get("costume_points_lederhosen", 0))
    }

    @Test
    fun `Claiming a costume spends a point on its next missing piece`() {
        val player = visit("iffie_claim", points = 2)

        // The piece children carry their item, so the click names the exact piece
        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("mime_mask", 1), slot = 1)
        tick()

        assertEquals(1, player.inventory.count("mime_mask"))
        assertEquals(1, player.get("costume_points", 0))
        assertEquals(1, player.get("costume_points_mime", 0), "Expected the shown balance refreshed")

        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("camo_helmet", 1), slot = 31)
        tick()

        assertEquals(1, player.inventory.count("camo_helmet"))
        assertEquals(0, player.get("costume_points", 0))
    }

    @Test
    fun `Outfit points combine with generic on the row and spend first`() {
        val player = createPlayer(Tile(3204, 3418), "iffie_locked")
        player["costume_points"] = 1
        player["mime_costume_points"] = 2
        val iffie = createNPC("iffie", Tile(3204, 3419))
        player.npcOption(iffie, "Claim-costume")
        tick(3)

        assertEquals(3, player.get("costume_points_mime", 0), "Expected locked + generic combined on the row")
        assertEquals(1, player.get("costume_points_camo", 0), "Expected only generic on other rows")

        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("mime_mask", 1), slot = 1)
        tick()

        assertEquals(1, player.inventory.count("mime_mask"))
        assertEquals(1, player.get("mime_costume_points", 0), "Expected the locked point spent first")
        assertEquals(1, player.get("costume_points", 0), "Expected the generic point kept")
    }

    @Test
    fun `Outfit points can't buy another outfit`() {
        val player = createPlayer(Tile(3204, 3418), "iffie_cross")
        player["mime_costume_points"] = 1
        val iffie = createNPC("iffie", Tile(3204, 3419))
        player.npcOption(iffie, "Claim-costume")
        tick(3)

        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("camo_top", 1), slot = 31)
        tick()

        assertTrue(player.inventory.isEmpty(), "Expected a mime point not to buy camo")
        assertEquals(1, player.get("mime_costume_points", 0))
    }

    @Test
    fun `A completed costume greys its row out`() {
        val player = visit("iffie_complete", points = 4)

        for (piece in listOf("camo_helmet", "camo_top", "camo_bottoms")) {
            player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item(piece, 1), slot = 31)
            tick()
        }

        assertTrue(player.get("costume_claimed_camo", false), "Expected the camo row greyed out")
        assertFalse(player.get("costume_claimed_mime", false))

        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("frog_mask", 1), slot = 11)
        tick()

        assertTrue(player.get("costume_claimed_frog_mask", false), "Expected the frog mask greyed out")
        assertFalse(player.get("costume_claimed_frog", false), "Expected the royal outfit still available")
    }

    @Test
    fun `No costume points means no costume`() {
        val player = visit("iffie_broke", points = 0)

        player.interfaceOption("costume_reward_select", "rewards", "Claim", item = Item("mime_mask", 1), slot = 1)
        tick()

        assertTrue(player.inventory.isEmpty())
        assertEquals(0, player.get("costume_points", 0))
    }

    @Test
    fun `Iffie explains costume points to newcomers`() {
        val player = createPlayer(Tile(3204, 3418), "iffie_chat")
        val iffie = createNPC("iffie", Tile(3204, 3419))

        player.npcOption(iffie, "Talk-to")
        tick(3)
        player.skipDialogues()
        player.dialogueOption(1) // "I've come for a random event costume."
        tick()
        player.skipDialogues()
        tick()

        assertFalse(player.interfaces.contains("costume_reward_select"), "Expected no store without points")
    }
}
