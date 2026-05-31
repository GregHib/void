package content.quest.member.rune_mysteries

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RuneMysteriesTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3210, 3223, 1))

        // Start quest at duke
        val dukeHoracio = NPCs.find(player.tile.regionLevel, "duke_horacio")
        player.npcOption(dukeHoracio, "Talk-to")
        tick(5)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("talisman_rune_mysteries"))
        assertEquals("started", player.quest("rune_mysteries"))

        // get package
        player.tele(3103, 9570, 0)
        val sedridor = NPCs.find(player.tile.regionLevel, "sedridor")
        player.npcOption(sedridor, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertEquals(1, player.inventory.count("research_package_rune_mysteries"))
        assertNull(player.dialogue)
        assertEquals("research_package", player.quest("rune_mysteries"))

        // deliever package
        player.tele(3252, 3401, 0)
        val aubury = NPCs.find(player.tile.regionLevel, "aubury")
        player.npcOption(aubury, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(3)
        player.fastForwardDialogue()

        assertEquals(1, player.inventory.count("research_notes_rune_mysteries"))
        assertNull(player.dialogue)
        assertEquals("research_notes", player.quest("rune_mysteries"))

        // deliver notes and finish quest
        player.tele(3103, 9570, 0)
        player.npcOption(sedridor, "Talk-to")
        tick()
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("completed", player.quest("rune_mysteries"))
        assertEquals(1, player.inventory.count("air_talisman"))
    }

    private fun Player.fastForwardDialogue() {
        assertNotNull(dialogue)
        require(suspension is Suspension.Continue)
        while (suspension is Suspension.Continue) {
            dialogueContinue()
        }
    }

    private fun Player.selectDialogueOption(option: Int) {
        assertNotNull(dialogue)
        require(suspension is Suspension.IntEntry)
        dialogueOption("line$option")
    }
}
