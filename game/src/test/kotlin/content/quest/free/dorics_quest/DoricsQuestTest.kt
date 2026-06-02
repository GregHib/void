package content.quest.free.dorics_quest

import WorldTest
import content.quest.quest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
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
        player.skipDialogues()
        // start quest with option 2
        player.dialogueOption(2)
        player.skipDialogues()
        // accept quest
        player.dialogueOption(1)
        player.skipDialogues()

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
        player.skipDialogues()
        // start quest with option 1
        player.dialogueOption(1)
        player.skipDialogues()
        // accept quest
        player.dialogueOption(1)
        player.skipDialogues()

        assertNull(player.dialogue)
        assertEquals("started", player.quest("dorics_quest"))

        // give player the required items
        player.inventory.add("clay", 6)
        player.inventory.add("copper_ore", 4)
        player.inventory.add("iron_ore", 2)

        // give items to doric and complete quest
        player.npcOption(doric, "Talk-to")
        tick()
        player.skipDialogues()

        tick(1)
        assertEquals("completed", player.quest("dorics_quest"))
    }
}
