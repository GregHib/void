package content.area.misthalin.draynor_village.wise_old_man

import FakeRandom
import WorldTest
import content.entity.player.bank.bank
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class WiseOldManTest : WorldTest() {

    @Test
    fun `Complete letter task`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 16) 12 else 0
            override fun nextInt(from: Int, until: Int) = from
        })
        val player = createPlayer(Tile(3088, 3254))
        player.levels.set(Skill.Prayer, 3)
        player["wise_old_man_met"] = true
        val wom = createNPC("wise_old_man_draynor", Tile(3088, 3255))
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(4)
        assertEquals("father_aereck", player["wise_old_man_npc", ""])
        assertEquals(1, player.inventory.count("old_mans_message"))
        val father = createNPC("father_aereck", Tile(3088, 3255))
        player.npcOption(father, "Talk-to")
        tick()
        player.dialogueContinue(4)
        assertEquals("", player["wise_old_man_npc", ""])
        assertEquals(1, player["wise_old_man_letters_completed", 0])
        assertEquals(0, player.inventory.count("old_mans_message"))
        assertEquals(215.0, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Complete basic task`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 100) 20 else 0
            override fun nextInt(from: Int, until: Int) = from
        })
        val player = createPlayer(Tile(3088, 3254))
        player["wise_old_man_met"] = true
        val wom = createNPC("wise_old_man_draynor", Tile(3088, 3255))
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(3)
        player.closeDialogue()
        assertEquals("beer_glass", player["wise_old_man_task", ""])
        assertEquals(3, player["wise_old_man_remaining", 0])
        player.inventory.add("beer_glass", 3)
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue(3)
        assertEquals(0, player.inventory.count("beer_glass"))
        assertEquals(1, player.inventory.count("uncut_red_topaz"))
        assertEquals("", player["wise_old_man_task", ""])
        assertEquals(0, player["wise_old_man_remaining", 0])
        assertEquals(1, player["wise_old_man_tasks_completed", 0])
    }

    @Test
    fun `Remove completed quest junk from bank`() {
        val player = createPlayer(Tile(3088, 3254))
        player["wise_old_man_met"] = true
        player["demon_slayer"] = "completed"
        player.bank.add("coins", 100)
        player.bank.add("silverlight_key_wizard_traiborn")
        player.bank.add("bronze_sword")
        player.bank.add("silverlight_key_sir_prysin")
        player["bank_tab_1"] = 2
        val wom = createNPC("wise_old_man_draynor", Tile(3088, 3255))
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(3)
        player.dialogueOption("line1")
        player.dialogueContinue()

        assertEquals(0, player.bank.count("silverlight_key_wizard_traiborn"))
        assertEquals(0, player.bank.count("silverlight_key_sir_prysin"))
        assertEquals("coins", player.bank[0].id)
        assertEquals("bronze_sword", player.bank[1].id)
        assertEquals(1, player["bank_tab_1", 0])
    }

    @Test
    fun `Keep junk from incomplete quests`() {
        val player = createPlayer(Tile(3088, 3254))
        player["wise_old_man_met"] = true
        player["demon_slayer"] = "key_hunt"
        player.inventory.add("silverlight_key_captain_rovin")
        player["pirates_treasure"] = "completed"
        player.inventory.add("pirate_message")
        val wom = createNPC("wise_old_man_draynor", Tile(3088, 3255))
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue(3)
        player.dialogueOption("line1")
        player.dialogueContinue()

        assertEquals(1, player.inventory.count("silverlight_key_captain_rovin"))
        assertEquals(0, player.inventory.count("pirate_message"))
    }

    @Test
    fun `Declining junk removal keeps items`() {
        val player = createPlayer(Tile(3088, 3254))
        player["wise_old_man_met"] = true
        player["pirates_treasure"] = "completed"
        player.inventory.add("pirate_message")
        val wom = createNPC("wise_old_man_draynor", Tile(3088, 3255))
        player.npcOption(wom, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue(3)
        player.dialogueOption("line2")

        assertEquals(1, player.inventory.count("pirate_message"))
    }
}
