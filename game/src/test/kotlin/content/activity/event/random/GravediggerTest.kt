package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import dialogueOption
import equipItem
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GravediggerTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val sites = listOf(
        Tile(1924, 4996),
        Tile(1926, 4999),
        Tile(1928, 4996),
        Tile(1930, 4999),
        Tile(1932, 4996),
    )

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "gravedigger")
        tick(10) // Leo appears, asks, and teleports the player away
        player.skipDialogues() // His explanation of the task
        tick()
        return player
    }

    private fun Player.graveAt(site: Int): GameObject = GameObjects.at(sites[site].add(instanceOffset()))
        .first { it.id.startsWith("gravedigger_grave_") || it.id.startsWith("gravedigger_open_grave_") }

    private fun Player.takeCoffin(site: Int) {
        objectOption(graveAt(site), "Take-coffin")
        tick(15) // Walk over and dig it up
    }

    private fun Player.buryCoffin(site: Int, coffin: String) {
        itemOnObject(graveAt(site), inventory.indexOf(coffin))
        tick(15)
    }

    private fun Player.leo(): NPC = NPCs.at(tile.regionLevel).first { it.id == "leo_gravedigger" }

    private fun Player.claimFinished() {
        npcOption(leo(), "Talk-to")
        tick(10) // Walk over to Leo
        skipDialogues() // "How are you getting on?"
        dialogueOption(1) // "There, finished!"
        tick()
        skipDialogues()
        tick(2)
    }

    private fun coffinName(index: Int) = if (index == 0) "coffin" else "coffin_${index + 1}"

    @Test
    fun `Leo kidnaps the player to a scrambled graveyard`() {
        val player = enter("gd_start")

        assertEquals("gravedigger", player.get<String>("random_event"))
        assertNotNull(player.instance())
        val contents = sites.indices.map { player.get("gravedigger_site_$it", 0) }
        assertTrue(contents.all { it in 1..5 }, "Expected every grave filled: $contents")
        assertFalse(contents.withIndex().all { it.value == it.index + 1 }, "Expected a scrambled arrangement")
        for (site in sites.indices) {
            assertEquals("gravedigger_grave_${contents[site] - 1}", player.graveAt(site).id)
        }
    }

    @Test
    fun `Take a coffin, check it and read the gravestone`() {
        val player = enter("gd_check")
        val contents = player.get("gravedigger_site_0", 0)

        player.takeCoffin(0)
        val coffin = coffinName(contents - 1)
        assertTrue(player.inventory.contains(coffin))
        assertEquals("gravedigger_open_grave_0", player.graveAt(0).id)

        player.equipItem(coffin, option = "Check")
        tick()
        assertTrue(player.interfaces.contains("gravedigger_coffin"))

        val stone = GameObjects.at(Tile(1924, 4998).add(player.instanceOffset())).first { it.id.startsWith("gravedigger_gravestone_") }
        player.objectOption(stone, "Read")
        tick(5)
        assertTrue(player.interfaces.contains("gravedigger_gravestone"))
    }

    @Test
    fun `A wrong arrangement earns a hint, the right one pays out`() {
        val player = enter("gd_solve")

        // Dig up all five coffins then claim it's done - Leo isn't fooled
        for (site in sites.indices) {
            player.takeCoffin(site)
        }
        player.claimFinished()
        assertEquals("gravedigger", player.get<String>("random_event"))

        // Rebury each in its proper grave
        for (site in sites.indices) {
            player.buryCoffin(site, coffinName(site))
        }
        player.claimFinished()

        assertTrue(player.inventory.contains("random_event_gift"), "Expected a random event gift")
        assertTrue(sites.indices.none { player.inventory.contains(coffinName(it)) }, "Expected Leo to keep his coffins")
        assertTrue(player.get("unlocked_emote_zombie_walk", false))
        assertTrue(player.get("unlocked_emote_zombie_dance", false))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Leaving early returns the player with nothing`() {
        val player = enter("gd_leave")
        player.takeCoffin(0)

        player.npcOption(player.leo(), "Talk-to")
        tick(10) // Walk over to Leo
        player.skipDialogues()
        player.dialogueOption(3) // "I want to leave."
        tick()
        player.skipDialogues()
        tick(2)

        assertTrue(player.inventory.isEmpty(), "Expected no reward and no coffins kept")
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Logging out mid-event puts the player back in the graveyard`() {
        val player = enter("gd_relog")
        val contents = player.get("gravedigger_site_0", 0)
        player.takeCoffin(0)
        val arrangement = sites.indices.map { player.get("gravedigger_site_$it", 0) }

        // A login restarts the active event, exactly like RandomEventKidnap.playerSpawn
        RandomEvents.start(player, "gravedigger")
        tick(8)

        assertNotNull(player.instance())
        assertEquals(arrangement, sites.indices.map { player.get("gravedigger_site_$it", 0) })
        assertEquals("gravedigger_open_grave_0", player.graveAt(0).id)
        assertTrue(player.inventory.contains(coffinName(contents - 1)), "Expected the dug-up coffin kept across relog")
    }
}
