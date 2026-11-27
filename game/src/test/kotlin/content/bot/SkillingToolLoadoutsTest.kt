package content.bot

import WorldTest
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.loadout.GearDefinitions
import content.bot.behaviour.loadout.GearItem
import content.entity.player.bank.bank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add

class SkillingToolLoadoutsTest : WorldTest() {

    private val manager: BotManager
        get() = scripts.filterIsInstance<BotCommands>().single().manager

    @Test
    fun `Pickaxes and hatchets need their skill level to use`() {
        assertEquals(listOf(Skill.Mining to 41), GearItem.of("rune_pickaxe")!!.useRequirements)
        assertEquals(listOf(Skill.Woodcutting to 41), GearItem.of("rune_hatchet")!!.useRequirements)
        assertEquals(7, GearDefinitions.get("pickaxes").items.size)
        assertEquals(7, GearDefinitions.get("hatchets").items.size)
    }

    @Test
    fun `Mining bots carry the best pickaxe they can use`() {
        assertEquals("bronze_pickaxe", boundTool("lumbridge_copper_mining", Skill.Mining, 10, "rune_pickaxe", "bronze_pickaxe"))
        assertEquals("rune_pickaxe", boundTool("lumbridge_copper_mining", Skill.Mining, 45, "rune_pickaxe", "bronze_pickaxe"))
    }

    @Test
    fun `Woodcutting bots carry the best hatchet they can use regardless of attack`() {
        assertEquals("bronze_hatchet", boundTool("draynor_north_trees_fletch_arrow_shaft", Skill.Woodcutting, 10, "rune_hatchet", "bronze_hatchet"))
        assertEquals("rune_hatchet", boundTool("draynor_north_trees_fletch_arrow_shaft", Skill.Woodcutting, 45, "rune_hatchet", "bronze_hatchet"))
    }

    private fun boundTool(activityId: String, skill: Skill, level: Int, vararg owned: String): String {
        val player = createPlayer()
        player.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
        for (id in owned) {
            player.bank.add(id)
        }
        val activity = manager.bind(Bot(player), manager.activity(activityId) ?: error("Missing activity $activityId"))
        val inventory = activity.setup.filterIsInstance<BotInventorySetup>().single()
        val tool = inventory.items.single { item -> item.ids.any { it.endsWith("_pickaxe") || it.endsWith("_hatchet") } }
        return tool.ids.single()
    }
}
