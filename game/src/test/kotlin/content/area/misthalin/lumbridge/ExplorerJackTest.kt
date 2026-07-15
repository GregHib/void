package content.area.misthalin.lumbridge

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExplorerJackTest : WorldTest() {

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(emptyTile, name)
        player["introducing_explorer_jack_task"] = "completed"
        val jack = createNPC("explorer_jack", emptyTile.addX(1))
        return player to jack
    }

    private fun askForRewards(player: Player, jack: NPC) {
        player.npcOption(jack, "Talk-to")
        tick(2)
        player.dialogueOption(2) // "Can I claim any rewards from you?"
        player.dialogueContinue(2) // repeated player line + "You certainly can!"
        player.dialogueOption(1) // "Inventory."
        player.dialogueContinue() // "I'll just fill your inventory with what you need, then."
        tick()
    }

    @Test
    fun `Claiming task rewards hands out the coins earned`() {
        val (player, jack) = setup("ej_claim")
        player["task_progress_overall"] = 5

        askForRewards(player, jack)

        assertEquals(50, player.inventory.count("coins"))
        assertEquals(5, player["task_progress_rewarded", 0])
    }

    @Test
    fun `Coins are priced by each task's own position, not the total progress`() {
        val (player, jack) = setup("ej_price")
        player["task_progress_overall"] = 8
        player["task_progress_rewarded"] = 5

        askForRewards(player, jack)

        // Tasks 5-7 pay 10 each; the old progress-based maths would have paid 40s
        assertEquals(30, player.inventory.count("coins"))
    }

    @Test
    fun `Asking for rewards again instead of clicking continue can't claim twice`() {
        val (player, jack) = setup("ej_dupe")
        player["task_progress_overall"] = 5

        askForRewards(player, jack)
        // Ignore the "There you go." continue and ask for the rewards again
        player.npcOption(jack, "Talk-to")
        tick(2)
        player.dialogueOption(2) // "Can I claim any rewards from you?"
        player.skipDialogues() // player line + Jack's answer
        if (player.dialogue == "dialogue_multi2") { // offered the claim menu again
            player.dialogueOption(1) // "Inventory."
            player.skipDialogues()
        }
        tick()

        assertEquals(50, player.inventory.count("coins"))
    }

    @Test
    fun `Nothing owed gets an apology instead of the claim menu`() {
        val (player, jack) = setup("ej_nothing")

        player.npcOption(jack, "Talk-to")
        tick()
        player.dialogueOption(2)
        player.dialogueContinue(2) // repeated player line + "Sorry, you're not owed any achievement rewards..."
        tick()

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Reward items are handed over and removed from what's owed`() {
        val (player, jack) = setup("ej_items")
        player.addVarbit("task_reward_items", "red_dye")

        askForRewards(player, jack)

        assertTrue(player.inventory.contains("red_dye"))
        assertFalse(player.containsVarbit("task_reward_items", "red_dye"))
    }

    @Test
    fun `Rewards that don't fit are held on to for later`() {
        val (player, jack) = setup("ej_full")
        player["task_progress_overall"] = 5
        player.inventory.add("logs", 28)

        askForRewards(player, jack)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(0, player["task_progress_rewarded", 0]) // still owed
    }
}
