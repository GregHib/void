package content.quest.member.druidic_ritual

import WorldTest
import content.quest.quest
import dialogueOption
import itemOnObject
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DruidicRitualTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(2925, 3485, 0))

        // Start the quest by talking to Kaqemeex at the Taverley stone circle
        val kaqemeex = NPCs.find(player.tile.regionLevel, "kaqemeex")
        player.npcOption(kaqemeex, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(2) // I'm in search of a quest.
        player.skipDialogues()
        player.dialogueOption(1) // Yes. (start the quest)
        player.skipDialogues()
        assertNull(player.dialogue)
        assertEquals("started", player.quest("druidic_ritual"))

        // Speak to Sanfew in Taverley to learn what's required
        player.tele(2896, 3426, 1)
        val sanfew = NPCs.find(player.tile.regionLevel, "sanfew")
        player.npcOption(sanfew, "Talk-to")
        tick()
        player.skipDialogues()
        player.dialogueOption(1) // I've been sent to help purify the Varrock stone circle.
        player.skipDialogues()
        player.dialogueOption(2) // Ok, I'll do that then.
        player.skipDialogues()
        assertNull(player.dialogue)
        assertEquals("cauldron", player.quest("druidic_ritual"))

        // Dip each raw meat in the Cauldron of Thunder in Taverley Dungeon
        player.inventory.add("raw_beef", "raw_rat_meat", "raw_bear_meat", "raw_chicken")
        player.tele(2892, 9831, 0)
        val cauldron = GameObjects.find(Tile(2893, 9831, 0), "cauldron_of_thunder")
        for ((raw, enchanted) in listOf(
            "raw_beef" to "enchanted_beef",
            "raw_rat_meat" to "enchanted_rat_meat",
            "raw_bear_meat" to "enchanted_bear_meat",
            "raw_chicken" to "enchanted_chicken",
        )) {
            player.itemOnObject(cauldron, player.inventory.indexOf(raw))
            tick(2)
            assertEquals(0, player.inventory.count(raw))
            assertEquals(1, player.inventory.count(enchanted))
        }

        // Hand the enchanted meats back to Sanfew
        player.tele(2896, 3426, 1)
        player.npcOption(sanfew, "Talk-to")
        tick()
        player.skipDialogues()
        assertNull(player.dialogue)
        assertEquals("kaqemeex", player.quest("druidic_ritual"))
        assertEquals(0, player.inventory.count("enchanted_beef"))
        assertEquals(0, player.inventory.count("enchanted_rat_meat"))
        assertEquals(0, player.inventory.count("enchanted_bear_meat"))
        assertEquals(0, player.inventory.count("enchanted_chicken"))

        // Claim the reward from Kaqemeex
        player.tele(2925, 3485, 0)
        player.npcOption(kaqemeex, "Talk-to")
        tick()
        player.skipDialogues()
        tick(2)
        assertEquals("completed", player.quest("druidic_ritual"))
        assertEquals(250.0, player.experience.get(Skill.Herblore))
        assertEquals(4, player["quest_points", 0])
    }
}
