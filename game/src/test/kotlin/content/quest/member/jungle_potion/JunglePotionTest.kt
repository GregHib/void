package content.quest.member.jungle_potion

import WorldTest
import content.entity.player.dialogue.continueDialogue
import content.quest.quest
import dialogueOption
import itemOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JunglePotionTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(2810, 3085))
        player.levels.set(Skill.Herblore, 3)
        val trufitus = NPCs.find(player.tile.regionLevel, "trufitus_tai_bwo_wannai")
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(3) // where is everyone?
        player.skipDialogues()
        player.dialogueOption(1) // Me? How can I help?
        player.skipDialogues()
        player.dialogueOption(1) // start quest
        player.skipDialogues()
        assertNull(player.dialogue)
        assertEquals("started", player.quest("jungle_potion"))


        // getting snake_weed
        player.tele(2762, 3044)
        val vine = GameObjects.find(Tile(2763, 3044), "snake_vine_full")
        player.interactObject(vine, "Search")
        tick(2)
        player.continueDialogue()
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("grimy_snake_weed"))
        assertEquals("found_snake_weed", player.quest("jungle_potion"))


        // clean the snake_weed
        player.itemOption("Clean", "grimy_snake_weed")
        tick(2)
        assertEquals(1, player.inventory.count("clean_snake_weed"))


        // giving snake_weed to trufitus
       // player.tele(2809, 3085)


    }
}
