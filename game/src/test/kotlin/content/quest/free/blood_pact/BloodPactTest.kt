package content.quest.free.blood_pact

import WorldTest
import content.entity.combat.hit.damage
import content.entity.effect.transform
import content.quest.instanceOffset
import content.quest.quest
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull

class BloodPactTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3245, 3198, 0))
        // Plenty of life to survive the cultists during the long combat sequence
        player.levels.set(Skill.Constitution, 99)

        // Start the quest by talking to Xenia in the Lumbridge cemetery
        val xenia = NPCs.find(Tile(3244, 3198, 0), "xenia")
        player.npcOption(xenia, "Talk-to")
        player.waitForDialogue() // "I'm glad you've come by. I need some help."
        player.skipDialogues()
        player.dialogueOption(1) // What do you need help with?
        player.skipDialogues() // Cultists explanation
        player.dialogueOption(1) // I'll help you.
        player.skipDialogues()
        assertEquals("quest_intro", player.menu)
        player.interfaceOption("quest_intro", "startyes_layer", "Yes")
        player.skipDialogues() // "I knew you would!" / "We've got no time to lose..."
        assertNull(player.dialogue)
        assertEquals("started", player.quest("blood_pact"))

        // Climb down into the catacombs, triggering the intro cutscene
        val stairs = GameObjects.find(Tile(3247, 3197, 0), "lumbridge_catacomb_stairs")
        player.objectOption(stairs, "Climb-down")
        player.runUntilIdle { player.quest("blood_pact") == "watched_cutscene" } // play out the cutscene
        assertEquals("watched_cutscene", player.quest("blood_pact"))

        val offset = player.instanceOffset()

        // Walking towards the first cultist wounds Xenia
        player.tele(offset.tile(3877, 5531, 1))
        player.runUntilIdle { player.quest("blood_pact") == "xenia_wounded" } // Xenia gets shot, fade out/in
        assertEquals("xenia_wounded", player.quest("blood_pact"))

        // Defeat and spare the first cultist (Kayle)
        player.tele(offset.tile(3876, 5543, 1))
        val kayle = NPCs.find(offset.tile(3877, 5543, 1), "kayle_attackable")
        player.defeat(kayle, "kayle_defeated")
        assertEquals("kayle", player.quest("blood_pact"))
        assertEquals("defeated", player["blood_pact_kayle", ""])

        player.talkAt(kayle, offset.tile(3877, 5542, 1), "Talk-to") // south of Kayle
        player.skipDialogues() // "Are - are you going to kill me?"
        player.dialogueOption(3) // No. Just give me your stuff and get out of here.
        player.runUntilIdle { player.quest("blood_pact") == "caitlin" } // Kayle flees, Xenia advice
        assertEquals("spared", player["blood_pact_kayle", ""])
        assertEquals("caitlin", player.quest("blood_pact"))

        // Defeat and spare the second cultist (Caitlin)
        player.tele(offset.tile(3865, 5538, 1))
        val caitlin = NPCs.find(offset.tile(3864, 5538, 1), "caitlin_attackable")
        player.defeat(caitlin, "caitlin_defeated")
        assertEquals("defeated", player["blood_pact_caitlin", ""])

        player.talkAt(caitlin, offset.tile(3863, 5538, 1), "Talk-to") // west of Caitlin (east edge is walled)
        player.skipDialogues() // "What are you waiting for? Finish me!"
        player.dialogueOption(3) // I'm not killing you. Just give me your stuff...
        player.runUntilIdle { player.quest("blood_pact") == "reese" } // Caitlin flees, Xenia advice
        assertEquals("spared", player["blood_pact_caitlin", ""])
        assertEquals("reese", player.quest("blood_pact"))

        // Defeat and kill the third cultist (Reese), completing the ritual
        player.tele(offset.tile(3866, 5525, 0))
        val reese = NPCs.find(offset.tile(3865, 5525, 0), "reese_attackable")
        player.defeat(reese, "reese_defeated")
        assertEquals("defeated", player["blood_pact_reese", ""])

        player.talkAt(reese, offset.tile(3864, 5525, 0), "Talk-to") // west of Reese
        player.skipDialogues() // "You've beaten me..." / "Now strike the final blow!"
        player.dialogueOption(2) // Time for you to die!
        player.runUntilIdle { player["blood_pact_reese", ""] == "killed" } // Reese dies, altar crumbles, Ilona calls out
        assertEquals("killed", player["blood_pact_reese", ""])

        // Untie the prisoner and escape to the surface
        val ilona = NPCs.find(offset.tile(3865, 5523, 0), "ilona_tied")
        player.talkAt(ilona, offset.tile(3864, 5523, 0), "Untie") // west of Ilona
        player.dialogueOption(1) // Yes, rescue Ilona.
        player.runUntilIdle { player.quest("blood_pact") == "untied_ilona" } // escape cutscene and Xenia's thanks
        assertEquals("untied_ilona", player.quest("blood_pact"))

        // Claim the reward from Xenia on the surface
        val xenia2 = NPCs.find(Tile(3245, 3198, 0), "xenia_2")
        player.npcOption(xenia2, "Talk-to")
        player.waitForDialogue() // "Is there anything you want to ask..."
        player.skipDialogues()
        player.dialogueOption(1) // I'm ready for my reward.
        player.skipDialogues() // "Farewell, adventurer."
        assertEquals("completed", player.quest("blood_pact"))
    }

    /**
     * Deals lethal damage to a cultist and ticks until it transforms into its defeated form.
     */
    private fun Player.defeat(npc: NPC, defeatedId: String) {
        npc.damage(1000, source = this)
        tickIf(50) { npc.transform != defeatedId }
    }

    /**
     * Walks up to [npc] on [tile] and opens the [option] interaction. A just-defeated cultist
     * spends a few ticks settling out of its death/combat state (and inherits the attackable
     * definition's aggression), so stay on this tile and keep it calm, retrying until the
     * conversation opens.
     */
    private fun Player.talkAt(npc: NPC, tile: Tile, option: String) {
        tele(tile)
        repeat(20) {
            npc.huntMode = ""
            npc.mode = EmptyMode
            mode = EmptyMode
            npcOption(npc, option)
            tick()
            if (dialogue != null) return
        }
        throw IllegalStateException("Could not '$option' ${npc.id} from $tile")
    }

    /**
     * Drives a cutscene / timed sequence with no branching choices: auto-clicks through any
     * continuable dialogue and ticks until [target] is reached and the player is fully idle
     * (no open dialogue, no active suspension, no pending queue). Stops as early as is safe.
     */
    private fun Player.runUntilIdle(limit: Int = 250, target: () -> Boolean) {
        var remaining = limit
        while (!(target() && dialogue == null && suspension == null && queue.isEmpty())) {
            if (dialogue != null && suspension is Suspension.Continue) {
                dialogueContinue()
            } else {
                tick()
            }
            if (remaining-- <= 0) {
                throw IllegalStateException(
                    "runUntilIdle exceeded $limit ticks (quest=${quest("blood_pact")}, dialogue=$dialogue, suspension=$suspension, queueEmpty=${queue.isEmpty()})",
                )
            }
        }
    }

    /**
     * Ticks until a dialogue is open, allowing the player to path to / start interacting with an NPC.
     */
    private fun Player.waitForDialogue() {
        tickIf(20) { dialogue == null }
    }
}
