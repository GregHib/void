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
        val trufitus = NPCs.findBySpawn(Tile(2809, 3086), "trufitus_tai_bwo_wannai")
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
        player.tele(2809, 3085)
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // Of course!
        player.skipDialogues()
        assertEquals("gave_snake_weed", player.quest("jungle_potion"))

       // getting ardrigal
        player.tele(2876, 3121)
        val palm = GameObjects.find(Tile(2877, 3120), "ardrigal_palm_full")
        player.interactObject(palm, "Search")
        tick(2)
        player.continueDialogue()
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("grimy_ardrigal"))
        assertEquals("found_ardrigal", player.quest("jungle_potion"))

        // clean the ardrigal
        player.itemOption("Clean", "grimy_ardrigal")
        tick(2)
        assertEquals(1, player.inventory.count("clean_ardrigal"))

        // giving ardrigal to trufitus
        player.tele(2809, 3085)
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // Of course!
        player.skipDialogues()
        assertEquals("gave_ardrigal", player.quest("jungle_potion"))

        // getting sito foil
        player.tele(2792, 3047)
        val soil = GameObjects.find(Tile(2791, 3047), "sito_soil_full")
        player.interactObject(soil, "Search")
        tick(2)
        player.continueDialogue()
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("grimy_sito_foil"))
        assertEquals("found_sito_foil", player.quest("jungle_potion"))

        // clean the sito foil
        player.itemOption("Clean", "grimy_sito_foil")
        tick(2)
        assertEquals(1, player.inventory.count("clean_sito_foil"))

        // giving sito foil to trufitus
        player.tele(2809, 3085)
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // Of course!
        player.skipDialogues()
        assertEquals("gave_sito_foil", player.quest("jungle_potion"))

        // getting volencia
        player.tele(2850, 3036)
        val rock = GameObjects.find(Tile(2851, 3036), "volencia_moss_rock_full")
        player.interactObject(rock, "Search")
        tick(2)
        player.continueDialogue()
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("grimy_volencia_moss"))
        assertEquals("found_volencia_moss", player.quest("jungle_potion"))

        // clean the volencia
        player.itemOption("Clean", "grimy_volencia_moss")
        tick(2)
        assertEquals(1, player.inventory.count("clean_volencia_moss"))

        // giving volencia to trufitus
        player.tele(2809, 3085)
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // Of course!
        player.skipDialogues()
        assertEquals("gave_volencia_moss", player.quest("jungle_potion"))

        // getting Rogue's Purse
        player.tele(2850, 9476)
        val wall = GameObjects.find(Tile(2850, 9475), "rogues_purse_cave_full")
        player.interactObject(wall, "Search")
        tick(6)
        player.continueDialogue()
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("grimy_rogues_purse"))
        assertEquals("found_rogues_purse", player.quest("jungle_potion"))

        // clean the Rogue's Purse
        player.itemOption("Clean", "grimy_rogues_purse")
        tick(2)
        assertEquals(1, player.inventory.count("clean_rogues_purse"))

        // giving Rogue's Purse to trufitus and finish the quest
        player.tele(2809, 3085)
        player.npcOption(trufitus, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // Of course!
        player.skipDialogues()
        assertEquals("completed", player.quest("jungle_potion"))
    }
}
