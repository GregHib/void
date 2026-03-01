package content.quest.free.imp_catcher

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class ImpCatcherTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete imp catcher`() {
        val player = createPlayer(Tile(3104, 3163, 2))
        val mizgog = NPCs.find(player.tile.regionLevel, "wizard_mizgog")
        player.npcOption(mizgog, "Talk-to")
        tick()
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue(6)
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        assertEquals("started", player.quest("imp_catcher"))
        assertNull(player.dialogue)

        player.inventory.add("black_bead", "white_bead", "yellow_bead", "red_bead")
        player.npcOption(mizgog, "Talk-to")
        tick()
        player.dialogueContinue(4)
        tick(16)
        assertEquals("completed", player.quest("imp_catcher"))
        assertEquals(27, player.inventory.spaces)
        assertEquals(1, player.inventory.count("amulet_of_accuracy"))
        assertEquals(875.0, player.experience.get(Skill.Magic))
    }
}
