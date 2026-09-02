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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuestCommandsTest : WorldTest() {

    @Test
    fun `Questprep meets a quest's skill, quest and item requirements`() {
        val admin = createPlayer(name = "prep admin")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "questprep rum_deal") }
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
    fun `Questprep hands out stacks of the items a quest needs more than one of`() {
        val admin = createPlayer(name = "prep admin myreque")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "questprep in_search_of_the_myreque") }
        tick()

        assertEquals(25, admin.levels.getMax(Skill.Agility))
        assertTrue(admin.questCompleted("nature_spirit"))
        assertTrue(admin.inventory.contains("steel_longsword"))
        assertTrue(admin.inventory.contains("steel_sword", 2))
        assertTrue(admin.inventory.contains("steel_nails", 225))
        assertTrue(admin.inventory.contains("plank", 6))
        assertTrue(admin.inventory.contains("druid_pouch_2", 5))
        assertTrue(admin.inventory.contains("silver_sickle_b"))
        assertTrue(admin.inventory.contains("coins", 10))
        assertTrue(admin.inventory.contains("hammer"))
    }

    @Test
    fun `Questreset clears the flags a quest keeps under other names`() {
        val admin = createPlayer(name = "reset admin myreque")
        admin.rights = PlayerRights.Admin
        admin["in_search_of_the_myreque"] = "completed"
        admin["bridgerung1"] = true
        admin["bridgerung3"] = true
        admin["route_bridgecomplete"] = true
        admin["thsfm_vanstrom_hide"] = true
        admin["met_sani"] = true
        admin["met_polmafi"] = true

        runTest { Commands.call(admin, "questreset in_search_of_the_myreque") }
        tick()

        assertEquals("unstarted", admin["in_search_of_the_myreque", "unstarted"])
        assertFalse(admin["bridgerung1", false])
        assertFalse(admin["bridgerung3", false])
        assertFalse(admin["route_bridgecomplete", false])
        assertFalse(admin["thsfm_vanstrom_hide", false], "Vanstrom sits back down in the tavern")
        assertFalse(admin["met_sani", false])
        assertFalse(admin["met_polmafi", false])
    }

    @Test
    fun `Questprep sets up the world state a quest inherits from its prerequisites`() {
        val admin = createPlayer(name = "prep admin biohazard")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "questprep biohazard") }
        tick()

        assertTrue(admin.questCompleted("plague_city"))
        assertTrue(admin.inventory.contains("priest_gown_top"))
        assertTrue(admin.inventory.contains("priest_gown_bottom"))
        assertTrue(admin["plaguecity_elena_at_home", false], "Elena should be home to start the quest")
    }

    @Test
    fun `Questprep never lowers a skill the player has already trained`() {
        val admin = createPlayer(name = "prep admin2")
        admin.rights = PlayerRights.Admin
        admin.experience.set(Skill.Fishing, Level.experience(80))

        runTest { Commands.call(admin, "questprep rum_deal") }
        tick()

        assertEquals(80, admin.levels.getMax(Skill.Fishing))
    }

    @Test
    fun `Questprep reports an unknown quest`() {
        val admin = createPlayer(name = "prep admin3")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "questprep not_a_quest") }
        tick()

        assertTrue(admin.containsMessage("No quest found with id 'not_a_quest'."))
    }

    @Test
    fun `Questreset puts the quest back to unstarted and clears its variables`() {
        val admin = createPlayer(name = "reset admin")
        admin.rights = PlayerRights.Admin
        admin["rum_deal"] = "collect_swill"
        admin["rum_deal_pressure_count"] = 5
        admin["rum_deal_slugling_count"] = 3
        admin["rum_deal_swab_a"] = 1
        admin["rum_deal_brewing_control"] = 2

        runTest { Commands.call(admin, "questreset rum_deal") }
        tick()

        assertEquals("unstarted", admin["rum_deal", "unstarted"])
        assertEquals(0, admin["rum_deal_pressure_count", 0])
        assertEquals(0, admin["rum_deal_slugling_count", 0])
        assertEquals(0, admin["rum_deal_swab_a", 0])
        assertEquals(0, admin["rum_deal_brewing_control", 0])
    }

    @Test
    fun `Questreset reports an unknown quest`() {
        val admin = createPlayer(name = "reset admin2")
        admin.rights = PlayerRights.Admin

        runTest { Commands.call(admin, "questreset not_a_quest") }
        tick()

        assertTrue(admin.containsMessage("No quest found with id 'not_a_quest'."))
    }
}
