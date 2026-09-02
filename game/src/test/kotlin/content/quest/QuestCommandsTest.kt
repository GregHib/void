package content.quest

import WorldTest
import containsMessage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestCommandsTest : WorldTest() {

    @Test
    fun `Quest prep meets a quest's skill, quest and item requirements`() {
        val admin = createPlayer(name = "prep admin")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "quest_prep rum_deal") }
        tick()

        // req_skills from quests.toml
        assertEquals(40, admin.levels.getMax(Skill.Farming))
        assertEquals(50, admin.levels.getMax(Skill.Fishing))
        assertEquals(47, admin.levels.getMax(Skill.Prayer))
        assertEquals(42, admin.levels.getMax(Skill.Crafting))
        assertEquals(42, admin.levels.getMax(Skill.Slayer))

        // req_quests
        assertTrue(admin.questCompleted("zogre_flesh_eaters"))

        // req_item_ids - everything else is handed out during the quest
        assertTrue(admin.inventory.contains("slayer_gloves"))
        assertTrue(admin.inventory.contains("spade"))
    }

    @Test
    fun `Quest prep never lowers a skill the player has already trained`() {
        val admin = createPlayer(name = "prep admin2")
        admin.rights = PlayerRights.Admin
        admin.experience.set(Skill.Fishing, Level.experience(80))

        runTest { Commands.call(admin, "quest_prep rum_deal") }
        tick()

        assertEquals(80, admin.levels.getMax(Skill.Fishing))
    }

    @Test
    fun `Quest prep reports an unknown quest`() {
        val admin = createPlayer(name = "prep admin3")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "quest_prep not_a_quest") }
        tick()

        assertTrue(admin.containsMessage("No quest found with id 'not_a_quest'."))
    }

    @Test
    fun `Quest reset puts the quest back to unstarted and clears its variables`() {
        val admin = createPlayer(name = "reset admin")
        admin.rights = PlayerRights.Admin
        admin["rum_deal"] = "collect_swill"
        admin["rum_deal_pressure_count"] = 5
        admin["rum_deal_slugling_count"] = 3
        admin["rum_deal_swab_a"] = 1
        admin["rum_deal_brewing_control"] = 2

        runTest { Commands.call(admin, "quest_reset rum_deal") }
        tick()

        assertEquals("unstarted", admin["rum_deal", "unstarted"])
        assertEquals(0, admin["rum_deal_pressure_count", 0])
        assertEquals(0, admin["rum_deal_slugling_count", 0])
        assertEquals(0, admin["rum_deal_swab_a", 0])
        assertEquals(0, admin["rum_deal_brewing_control", 0])
    }

    @Test
    fun `Quest reset reports an unknown quest`() {
        val admin = createPlayer(name = "reset admin2")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "quest_reset not_a_quest") }
        tick()

        assertTrue(admin.containsMessage("No quest found with id 'not_a_quest'."))
    }
}
