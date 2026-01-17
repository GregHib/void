package content.entity.player.stat

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*

class Stats(val definitions: InterfaceDefinitions) : Script {

    init {
        interfaceOpened("stats") {
            sendVariable("skill_stat_flash")
            Skill.entries.forEach {
                experience.update(it)
            }
        }

        interfaceOption("View", id = "stats:*") {
            val skill = Skill.valueOf(it.component.toSentenceCase())
            openGuide(this, skill)
        }

        interfaceOption("Open subsection", id = "skill_guide:*") {
            val index = (definitions.getComponent(it.id, it.component)?.index ?: 0) - 10
            val menuIndex = get("active_skill_guide", 1)
            set("skill_guide", menuIndex + index * 1024)
        }
    }

    companion object {
        private val menu = listOf(
            Attack, Strength, Ranged, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting,
            Mining, Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering,
        )
        fun openGuide(player: Player, skill: Skill, subsection: Int = 0) {
            val menuIndex = menu.indexOf(skill) + 1
            player.closeInterfaces()
            if (player.containsVarbit("skill_stat_flash", skill.name.lowercase())) {
                val extra = 0 // 0 - normal, 2 - combat milestone, 4 - total milestone
                player["level_up_details"] = menuIndex * 8 + extra
                player.open("skill_level_details")
                player.removeVarbit("skill_stat_flash", skill.name.lowercase())
            } else {
                player["skill_guide"] = menuIndex + subsection * 1024
                player["active_skill_guide"] = menuIndex
                player.open("skill_guide")
            }
        }
    }
}
