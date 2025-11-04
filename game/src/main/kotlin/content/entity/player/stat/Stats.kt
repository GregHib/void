package content.entity.player.stat

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.inject

class Stats : Script {

    val definitions: InterfaceDefinitions by inject()

    val menu = listOf(
        Attack, Strength, Ranged, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting,
        Mining, Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering,
    )

    init {
        interfaceOpen("stats") {
            sendVariable("skill_stat_flash")
            Skill.entries.forEach {
                experience.update(it)
            }
        }

        interfaceOption("View", id = "stats:*") {
            val skill = valueOf(it.component.toSentenceCase())
            val menuIndex = menu.indexOf(skill) + 1
            closeInterfaces()
            if (containsVarbit("skill_stat_flash", skill.name.lowercase())) {
                val extra = 0 // 0 - normal, 2 - combat milestone, 4 - total milestone
                set("level_up_details", menuIndex * 8 + extra)
                open("skill_level_details")
                removeVarbit("skill_stat_flash", skill.name.lowercase())
            } else {
                set("skill_guide", menuIndex)
                set("active_skill_guide", menuIndex)
                open("skill_guide")
            }
        }

        interfaceOption("Open subsection", id = "skill_guide:*") {
            val index = (definitions.getComponent(it.id, it.component)?.index ?: 0) - 10
            val menuIndex = get("active_skill_guide", 1)
            set("skill_guide", menuIndex + index * 1024)
        }
    }
}
