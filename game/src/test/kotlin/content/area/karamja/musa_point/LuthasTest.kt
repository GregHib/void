package content.area.karamja.musa_point

import WorldTest
import containsMessage
import content.entity.player.dialogue.continueDialogue
import intEntry
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LuthasTest : WorldTest() {

    @Test
    fun `Take the plantation job`() {
        val player = createPlayer(Tile(2939, 3155))
        val luthas = createNPC("luthas_musa_point", Tile(2939, 3154))

        player.takeJob(luthas)

        assertTrue(player["banana_plantation_job", false])
    }

    @Test
    fun `Ask about the customs officer`() {
        val player = createPlayer(Tile(2939, 3155))
        val luthas = createNPC("luthas_musa_point", Tile(2939, 3154))

        player.npcOption(luthas, "Talk-to")
        tickIf(limit = 20) { player.suspension == null }
        player.continueDialogue()
        player.intEntry(2)
        repeat(4) { player.continueDialogue() }

        assertFalse(player["banana_plantation_job", false])
    }

    @Test
    fun `Fill the crate and get paid`() {
        val player = createPlayer(Tile(2943, 3152))
        val luthas = createNPC("luthas_musa_point", Tile(2939, 3154))
        player.takeJob(luthas)
        player.inventory.add("banana", 12)
        val crate = GameObjects.find(CRATE, "crate_14")

        player.objectOption(crate, "Fill")
        tick(10)

        assertEquals(10, player["banana_crate_bananas", 0])
        assertEquals(2, player.inventory.count("banana"))

        player.npcOption(luthas, "Talk-to")
        tickIf(limit = 20) { player.suspension == null }
        repeat(2) { player.continueDialogue() }

        assertEquals(30, player.inventory.count("coins"))
        assertEquals(0, player["banana_crate_bananas", 0])
        assertFalse(player["banana_plantation_job", false])
    }

    @Test
    fun `Pack a single banana onto the crate`() {
        val player = createPlayer(Tile(2943, 3152))
        val luthas = createNPC("luthas_musa_point", Tile(2939, 3154))
        player.takeJob(luthas)
        player.inventory.add("banana")
        val crate = GameObjects.find(CRATE, "crate_14")

        player.itemOnObject(crate, player.inventory.indexOf("banana"))
        tick(10)

        assertEquals(1, player["banana_crate_bananas", 0])
        assertFalse(player.inventory.contains("banana"))
    }

    @Test
    fun `Crate does nothing without the job`() {
        val player = createPlayer(Tile(2943, 3152))
        player.inventory.add("banana", 5)
        val crate = GameObjects.find(CRATE, "crate_14")

        player.objectOption(crate, "Fill")
        tick(10)

        assertEquals(0, player["banana_crate_bananas", 0])
        assertEquals(5, player.inventory.count("banana"))
        assertTrue(player.containsMessage("I don't know what goes in there."))
    }

    private fun Player.takeJob(luthas: world.gregs.voidps.engine.entity.character.npc.NPC) {
        npcOption(luthas, "Talk-to")
        tickIf(limit = 20) { suspension == null }
        continueDialogue()
        intEntry(1)
        repeat(4) { continueDialogue() }
    }

    companion object {
        private val CRATE = Tile(2943, 3151)
    }
}
