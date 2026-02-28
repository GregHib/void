package content.area.misthalin.draynor_village.wise_old_man

import FakeRandom
import WorldTest
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
}
