package content.quest.member.creature_of_fenkenstrain

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOnItem
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreatureOfFenkenstrainTest : WorldTest() {

    @Test
    fun `Complete creature of fenkenstrain quest`() {
        val player = createPlayer(Tile(3488, 3486))
        player.experience.set(Skill.Thieving, Level.experience(25))
        player["priest_in_peril"] = "completed"
        player["the_restless_ghost"] = "completed"

        // Read signpost in Canifis → sets fenk_read_signpost flag
        val signpost = GameObjects.find(Tile(3488, 3485), "fenk_signpost")
        player.objectOption(signpost, "Read")
        tick(2)
        player.dialogueContinue()
        assertTrue(player["fenk_read_signpost", false])
        assertEquals("unstarted", player["creature_of_fenkenstrain", "unstarted"])

        // Stage unstarted → body_parts: Fenkenstrain's butler interview
        player.tele(3551, 3550)
        val fenkenstrain = createNPC("dr_fenkenstrain", Tile(3551, 3551))
        player.npcOption(fenkenstrain, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Have you come to apply for the job?"
        player.dialogueOption("line1") // Yes.
        player.dialogueContinue() // player "Yes, if it pays well."
        player.dialogueContinue() // npc "I'll have to ask you some questions first."
        player.dialogueContinue() // player "Okay..."
        player.dialogueContinue() // npc "How would you describe yourself in one word?"
        player.dialogueOption("line4") // Braindead
        player.dialogueContinue() // npc "Mmmm, I see."
        player.dialogueContinue() // npc "Just one more question..."
        player.dialogueOption("line4") // Grave-digging → offerJob()
        player.dialogueContinue(14) // npc accept + full job briefing
        assertEquals("body_parts", player["creature_of_fenkenstrain", "unstarted"])

        // Collect body parts; combine empty head with pickled brain
        player.inventory.add("fenk_torso")
        player.inventory.add("fenk_arms")
        player.inventory.add("fenk_legs")
        player.inventory.add("fenk_brain")
        player.inventory.add("fenk_head_empty")
        val headSlot = player.inventory.indexOf("fenk_head_empty")
        val brainSlot = player.inventory.indexOf("fenk_brain")
        player.itemOnItem(headSlot, brainSlot)
        tick(2)
        player.dialogueContinue() // item dialogue "You squeeze the pickled brain..."
        assertTrue(player.inventory.contains("fenk_head_full"))
        assertFalse(player.inventory.contains("fenk_head_empty"))
        assertFalse(player.inventory.contains("fenk_brain"))

        // Stage body_parts → sewing: deliver all parts to Fenkenstrain
        player.npcOption(fenkenstrain, "Talk-to")
        tick(2)
        player.dialogueOption("line1") // I have some body parts for you. (option<Neutral>)
        player.dialogueContinue(8) // player chathead + 4x part accepts + 3x sewingTransition
        assertEquals("sewing", player["creature_of_fenkenstrain", "unstarted"])
        assertFalse(player.inventory.contains("fenk_torso"))

        // Stage sewing → conductor: give needle and 5 thread
        player.inventory.add("needle")
        player.inventory.add("thread", 5)
        player.npcOption(fenkenstrain, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Where are my needle and thread?"
        player.dialogueContinue() // npc "Ah, a needle. Wonderful."
        player.dialogueContinue() // npc "Some thread, excellent."
        player.dialogueContinue(10) // sewLifeFromLightning (statement + 9 chathead lines)
        assertEquals("conductor", player["creature_of_fenkenstrain", "unstarted"])
        assertFalse(player.inventory.contains("needle"))
        assertEquals(0, player.inventory.count("thread"))

        // Stage conductor → creature_alive: repair the broken conductor on the balcony
        player.inventory.add("fenk_conductor")
        player.tele(3548, 3537, 2)
        val brokenConductor = GameObjects.find(Tile(3548, 3536, 2), "fenk_conductor_broken")
        player.objectOption(brokenConductor, "Repair")
        tick(2)
        player.dialogueContinue() // statement "You repair the lightning conductor..."
        assertEquals("creature_alive", player["creature_of_fenkenstrain", "unstarted"])
        assertFalse(player.inventory.contains("fenk_conductor"))

        // Stage creature_alive → creature_loose: Fenkenstrain panics and gives tower key
        player.tele(3551, 3550)
        player.npcOption(fenkenstrain, "Talk-to")
        tick(2)
        player.dialogueContinue(11) // creatureRampage: full dialogue, key given at end
        assertEquals("creature_loose", player["creature_of_fenkenstrain", "unstarted"])
        assertTrue(player.inventory.contains("fenk_tower_key"))

        // Stage creature_loose → creature_convinced: meet the creature in the tower
        player.tele(3547, 3554, 2)
        val monster = createNPC("fenkenstrains_monster", Tile(3547, 3555, 2))
        player.npcOption(monster, "Talk-to")
        tick(2)
        player.dialogueContinue(20) // firstMeeting: full history dialogue
        assertEquals("creature_convinced", player["creature_of_fenkenstrain", "unstarted"])

        // Stage creature_convinced → completed: pickpocket ring_of_charos from Fenkenstrain
        player.tele(3551, 3550)
        player.npcOption(fenkenstrain, "Pickpocket")
        tick(2)
        assertEquals("completed", player["creature_of_fenkenstrain", "unstarted"])
        assertEquals(2, player["quest_points", 0])
        assertTrue(player.inventory.contains("ring_of_charos"))
    }

    @Test
    fun `Chest does not give duplicate mausoleum key when one is already in inventory`() {
        val player = createPlayer(Tile(3501, 9967))
        player.inventory.add("fenk_mausoleum_key")
        val chest = GameObjects.find(Tile(3500, 9967), "fenk_chest_open")

        player.objectOption(chest, "Search")
        tick(2)

        assertEquals(1, player.inventory.count("fenk_mausoleum_key"))
        assertTrue(player.containsMessage("The chest is empty."))
    }

    @Test
    fun `Chest gives mausoleum key on first open`() {
        val player = createPlayer(Tile(3501, 9967))
        val chest = GameObjects.find(Tile(3500, 9967), "fenk_chest_open")

        player.objectOption(chest, "Search")
        tick(2)
        player.dialogueContinue()

        assertEquals(1, player.inventory.count("fenk_mausoleum_key"))
    }

    @Test
    fun `Digging grave before quest start gives empty message`() {
        val player = createPlayer(Tile(3503, 3576))
        player.inventory.add("spade")
        val grave = GameObjects.find(Tile(3502, 3576), "fenk_grave")

        player.objectOption(grave, "Dig")
        tick(10)

        assertTrue(player.containsMessage("...but the grave is empty."))
        assertFalse(player.inventory.contains("fenk_torso"))
    }

    @Test
    fun `Digging graves yields body parts during body_parts stage`() {
        val player = createPlayer(Tile(3503, 3576))
        player["creature_of_fenkenstrain"] = "body_parts"
        player.inventory.add("spade")

        val torsoGrave = GameObjects.find(Tile(3502, 3576), "fenk_grave")
        player.objectOption(torsoGrave, "Dig")
        tick(10)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("fenk_torso"))

        player.tele(3504, 3576)
        val armsGrave = GameObjects.find(Tile(3504, 3577), "fenk_grave")
        player.objectOption(armsGrave, "Dig")
        tick(10)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("fenk_arms"))

        player.tele(3505, 3576)
        val legsGrave = GameObjects.find(Tile(3506, 3576), "fenk_grave")
        player.objectOption(legsGrave, "Dig")
        tick(10)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("fenk_legs"))

        player.tele(3608, 3490)
        val headGrave = GameObjects.find(Tile(3608, 3491), "fenk_grave_poor")
        player.objectOption(headGrave, "Dig")
        tick(10)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("fenk_head_empty"))
    }

    @Test
    fun `Combining head and brain creates full head`() {
        val player = createPlayer(Tile(3488, 3486))
        player.inventory.add("fenk_head_empty")
        player.inventory.add("fenk_brain")

        player.itemOnItem(player.inventory.indexOf("fenk_head_empty"), player.inventory.indexOf("fenk_brain"))
        tick(2)
        player.dialogueContinue()

        assertTrue(player.inventory.contains("fenk_head_full"))
        assertFalse(player.inventory.contains("fenk_head_empty"))
        assertFalse(player.inventory.contains("fenk_brain"))
    }

    @Test
    fun `Combining marble and obsidian amulets creates star amulet`() {
        val player = createPlayer(Tile(3488, 3486))
        player.inventory.add("fenk_marble_amulet")
        player.inventory.add("fenk_obsidian_amulet")

        player.itemOnItem(
            player.inventory.indexOf("fenk_marble_amulet"),
            player.inventory.indexOf("fenk_obsidian_amulet"),
        )
        tick(2)
        player.dialogueContinue()

        assertTrue(player.inventory.contains("fenk_star_amulet"))
        assertFalse(player.inventory.contains("fenk_marble_amulet"))
        assertFalse(player.inventory.contains("fenk_obsidian_amulet"))
    }

    @Test
    fun `Fenkenstrain rejects interview choices that are not braindead or grave-digging`() {
        val player = createPlayer(Tile(3551, 3550))
        val fenkenstrain = createNPC("dr_fenkenstrain", Tile(3551, 3551))

        player.npcOption(fenkenstrain, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Have you come to apply for the job?"
        player.dialogueOption("line1") // Yes.
        player.dialogueContinue() // player "Yes, if it pays well."
        player.dialogueContinue() // npc "I'll have to ask you some questions first."
        player.dialogueContinue() // player "Okay..."
        player.dialogueContinue() // npc "How would you describe yourself..."
        player.dialogueOption("line1") // Stunning → rejected()
        player.dialogueContinue(2) // npc "Mmmm, I see." + "I'm sorry, but I don't think..."

        assertEquals("unstarted", player["creature_of_fenkenstrain", "unstarted"])
    }

    @Test
    fun `Pickpocket requires creature_convinced stage`() {
        val player = createPlayer(Tile(3551, 3550))
        player.experience.set(Skill.Thieving, Level.experience(25))
        player["creature_of_fenkenstrain"] = "conductor"
        val fenkenstrain = createNPC("dr_fenkenstrain", Tile(3551, 3551))

        player.npcOption(fenkenstrain, "Pickpocket")
        tick(2)
        player.dialogueContinue() // npc "What do you think you're doing???"

        assertFalse(player.inventory.contains("ring_of_charos"))
        assertEquals("conductor", player["creature_of_fenkenstrain", "unstarted"])
    }
}
