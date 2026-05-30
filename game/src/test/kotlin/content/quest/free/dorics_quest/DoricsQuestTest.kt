package content.quest.free.dorics_quest

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
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DoricsQuestTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Start quest option 2`() {
        val player = createPlayer(Tile(3208, 3215, 0))

        player.levels.set(Skill.Mining, 15)

        // talk to doric
        player.tele(2951, 3450)
        val doric = NPCs.find(player.tile.regionLevel, "doric")
        player.npcOption(doric, "Talk-to")
        tick()
        player.fastForwardDialogue()
        // start quest with option 2
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        // accept quest
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("started", player.quest("dorics_quest"))
    }

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3208, 3215, 0))

        player.levels.set(Skill.Mining, 15)

        // talk to doric
        player.tele(2951, 3450)
        val doric = NPCs.find(player.tile.regionLevel, "doric")
        player.npcOption(doric, "Talk-to")
        tick()
        player.fastForwardDialogue()
        // start quest with option 1
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        // accept quest
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("started", player.quest("dorics_quest"))

        // give player the required items
        player.inventory.add("clay", 6)
        player.inventory.add("copper_ore", 4)
        player.inventory.add("iron_ore", 2)

        // give items to doric and complete quest
        player.npcOption(doric, "Talk-to")
        tick()
        player.fastForwardDialogue()

        tick(1)
        assertEquals("completed", player.quest("dorics_quest"))
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
