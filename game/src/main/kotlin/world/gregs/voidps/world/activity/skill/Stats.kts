package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentIntId
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toSentenceCase

val definitions: InterfaceDefinitions by inject()

val menu = listOf(Attack, Strength, Ranged, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting,
    Mining, Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering)

on<InterfaceOpened>({ id == "stats" }) { player: Player ->
    player.sendVar("skill_stat_flash")
    values().forEach {
        player.experience.update(it)
    }
}

on<InterfaceOption>({ id == "stats" && option == "View" }) { player: Player ->
    val skill = valueOf(component.toSentenceCase())
    val menuIndex = menu.indexOf(skill) + 1

    if (player.hasVar("skill_stat_flash", skill.name)) {
        val extra = 0//0 - normal, 2 - combat milestone, 4 - total milestone
        player.setVar("level_up_details", menuIndex * 8 + extra)
        player.open("skill_level_details")
        player.removeVar("skill_stat_flash", skill.name)
    } else {
        player.setVar("skill_guide", menuIndex)
        player["active_skill_guide"] = menuIndex
        player.open("skill_guide")
    }
}

on<InterfaceOption>({ id == "skill_guide" && option == "Open subsection" }) { player: Player ->
    val definition = definitions.get(id)
    val index = (definition.getComponentIntId(component) ?: 0) - 10
    val menuIndex = player["active_skill_guide", 1]
    player.setVar("skill_guide", menuIndex + index * 1024)
}