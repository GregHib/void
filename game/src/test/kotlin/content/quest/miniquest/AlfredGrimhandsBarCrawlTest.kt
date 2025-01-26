package content.quest.miniquest

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import WorldTest
import dialogueOption
import npcOption
import objectOption
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class AlfredGrimhandsBarCrawlTest : WorldTest() {

    @Test
    fun `Complete the mini-quest`() {
        val player = createPlayer("quester", Tile(2542, 3569))
        player.inventory.add("coins", 1000)
        for (skill in Skill.entries) {
            player.levels.set(skill, 50)
        }
        assertEquals("unstarted", player["alfred_grimhands_barcrawl", "unstarted"])

        val guard = findNpc(player, "barbarian_guard")
        player.npcOption(guard, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line1") // I want to come through this gate
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line2") // Looks can be deceiving
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals("signatures", player["alfred_grimhands_barcrawl", "unstarted"])
        assertTrue(player.inventory.contains("barcrawl_card"))
        assertTrue(player["barcrawl_signatures", emptyList<String>()].isEmpty())

        // Blue moon inn
        player.tele(3223, 3400)
        var bartender = findNpc(player, "bartender_blue_moon_inn")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line3") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(950, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "uncle_humphreys_gutrot"))


        // Blurberry's bar
        player.tele(2480, 3490, 1)
        bartender = findNpc(player, "blurberry")
        player.npcOption(bartender, 0) // Talk-to
        tick(2)
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line2") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(940, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "fire_toad_blast"))


        // Blurberry's bar
        player.tele(2480, 3490, 1)
        bartender = findNpc(player, "blurberry")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line2") // Doing bar crawl
        player.dialogueOption("continue")
        assertEquals(940, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "fire_toad_blast"))


        // Blurberry's bar
        player.tele(2480, 3490, 1)
        bartender = findNpc(player, "blurberry")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line2") // Doing bar crawl
        player.dialogueOption("continue")
        assertEquals(940, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "fire_toad_blast"))


        // Dead man's chest
        player.tele(2794, 3155, 0)
        bartender = findNpc(player, "bartender_dead_mans_chest")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line4") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(925, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "supergrog"))


        // Dragon Inn
        player.tele(2556, 3079)
        bartender = findNpc(player, "bartender_dragon_inn")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line5") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(913, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "fire_brandy"))


        // Flying Horse Inn
        player.tele(2575, 3319)
        bartender = findNpc(player, "bartender_flying_horse_inn")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("line3") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(905, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "heart_stopper"))


        // Forester's Arms
        player.tele(2691, 3493)
        bartender = findNpc(player, "bartender_foresters_arms")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line3") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(887, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "liverbane_ale"))


        // Jolly Boar Inn
        player.tele(3278, 3488)
        bartender = findNpc(player, "bartender_jolly_boar_inn")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line4") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(877, player.inventory.count("coins"))
        tick(12)
        player.dialogueOption("continue")
        assertTrue(player.containsVarbit("barcrawl_signatures", "olde_suspiciouse"))


        // Karamja Spirits Bar
        player.tele(2927, 3146)
        bartender = findNpc(player, "bartender_zambo")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line3") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(870, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "ape_bite_liqueur"))


        // Rising Sun Inn
        player.tele(2957, 3370)
        bartender = findNpc(player, "barmaid_kaylee")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("line2") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(800, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "hand_of_death_cocktail"))


        // Rusty Anchor
        player.tele(3045, 3256)
        bartender = findNpc(player, "bartender_rusty_anchor")
        player.npcOption(bartender, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("line3") // Doing bar crawl
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals(792, player.inventory.count("coins"))
        tick(12)
        assertTrue(player.containsVarbit("barcrawl_signatures", "black_skull_ale"))


        // Return to guard
        player.tele(2542, 3569)
        player.npcOption(guard, 0) // Talk-to
        tickIf { player.dialogue == null }
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        player.dialogueOption("continue")
        assertEquals("completed", player["alfred_grimhands_barcrawl", "unstarted"])
    }

    @Test
    fun `Can't enter gate before quest is completed`() {
        val player = createPlayer("quester", Tile(2545, 3569))

        val gate = objects[Tile(2545, 3569), "barbarian_outpost_gate_right_closed"]!!
        player.objectOption(gate, "Open")
        tick(4)
        assertNotEquals(Tile(2546), player.tile)
    }

    @Test
    fun `Can enter gate after quest completed`() {
        val player = createPlayer("quester", Tile(2545, 3569))
        player["alfred_grimhands_barcrawl"] = "completed"

        val gate = objects[Tile(2545, 3569), "barbarian_outpost_gate_right_closed"]!!
        player.objectOption(gate, "Open")
        tick(4)
        assertEquals(Tile(2546, 3569), player.tile)
    }

    private fun findNpc(player: Player, id: String) = npcs[player.tile.region.toLevel(player.tile.level)].first { it.id == id }

}