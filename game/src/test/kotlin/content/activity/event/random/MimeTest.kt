package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeTest : WorldTest() {

    private val stage = Tile(2008, 4762)

    private fun Player.performEmote(emote: String) {
        val player = this
        Script.launch { InterfaceApi.option(player, InterfaceOption(Item.EMPTY, -1, emote, 0, "emotes:$emote")) }
        tick(6) // let the emote animation + copy check resolve
    }

    private fun enter(name: String, origin: Tile = Tile(3221, 3218)): Player {
        val player = createPlayer(stage, name)
        player["random_event"] = "mime"
        player["random_event_origin"] = origin.id
        player["mime_correct"] = 0
        player["mime_emote"] = "think"
        return player
    }

    @Test
    fun `Event kidnaps the player to the stage and assigns an emote`() {
        val player = createPlayer(Tile(3221, 3218), "mime_start")
        RandomEvents.start(player, "mime")
        tick(12)

        assertEquals("mime", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertTrue(player.get<String>("mime_emote") != null)
        val off = player.instanceOffset()
        assertNotNull(NPCs.firstOrNull(Tile(2011, 4762).add(off.x, off.y)) { it.id == "mime" })
    }

    @Test
    fun `Copying the mime's emote counts, a wrong one does not`() {
        val player = enter("mime_copy")

        player.performEmote("dance") // not the assigned "think"
        tick()
        assertEquals(0, player.get("mime_correct", 0))

        player.performEmote("think")
        tick()
        assertEquals(1, player.get("mime_correct", 0))
    }

    @Test
    fun `Four correct emotes unlock the mime emotes, reward a costume and return the player`() {
        val origin = Tile(3221, 3218)
        val player = enter("mime_finish", origin)

        repeat(4) {
            player.performEmote(player.get<String>("mime_emote")!!)
            tick()
        }
        tick(2)

        assertTrue(player["unlocked_emote_glass_wall", false])
        assertTrue(player["unlocked_emote_lean", false])
        assertEquals(1, player.inventory.count("mime_mask"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }

    @Test
    fun `Owning the full mime costume rewards coins`() {
        val player = enter("mime_coins")
        for (piece in listOf("mime_mask", "mime_top", "mime_legs", "mime_gloves", "mime_boots")) {
            player.inventory.add(piece)
        }

        repeat(4) {
            player.performEmote(player.get<String>("mime_emote")!!)
            tick()
        }
        tick(2)

        assertEquals(500, player.inventory.count("coins"))
    }
}
