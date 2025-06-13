package content.entity.player.stat

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.inject

val definitions: InterfaceDefinitions by inject()

val menu = listOf(
    Attack, Strength, Ranged, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting,
    Mining, Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering,
)

interfaceOpen("stats") { player ->
    player.sendVariable("skill_stat_flash")
    Skill.entries.forEach {
        player.experience.update(it)
    }
}

interfaceOption("View", id = "stats") {
    val skill = valueOf(component.toSentenceCase())
    val menuIndex = menu.indexOf(skill) + 1
    player.closeInterfaces()
    if (player.containsVarbit("skill_stat_flash", skill.name.lowercase())) {
        val extra = 0 // 0 - normal, 2 - combat milestone, 4 - total milestone
        player["level_up_details"] = menuIndex * 8 + extra
        player.open("skill_level_details")
        player.removeVarbit("skill_stat_flash", skill.name.lowercase())
    } else {
        player["skill_guide"] = menuIndex
        player["active_skill_guide"] = menuIndex
        player.open("skill_guide")
    }
}

interfaceOption("Open subsection", id = "skill_guide") {
    val index = (definitions.getComponent(id, component)?.index ?: 0) - 10
    val menuIndex = player["active_skill_guide", 1]
    player["skill_guide"] = menuIndex + index * 1024
}
