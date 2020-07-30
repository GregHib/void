package rs.dusk.world.activity.skill

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.SkillLevelMessage
import rs.dusk.world.activity.skill.Skill.*
import rs.dusk.world.interact.player.display.InterfaceInteraction

val menu = listOf(ATTACK, STRENGTH, RANGE, MAGIC, DEFENCE, CONSTITUTION, PRAYER, AGILITY, HERBLORE, THIEVING, CRAFTING, RUNECRAFTING,
    MINING, SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, FLETCHING, SLAYER, FARMING, CONSTRUCTION, HUNTER, SUMMONING, DUNGEONEERING)

BitwiseVariable(1179, Variable.Type.VARP, true, values = listOf(
    ATTACK, STRENGTH, DEFENCE, RANGE, PRAYER, MAGIC, CONSTITUTION, AGILITY, HERBLORE, THIEVING, CRAFTING, FLETCHING, MINING,
    SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, RUNECRAFTING, SLAYER, FARMING, CONSTRUCTION, HUNTER, SUMMONING, DUNGEONEERING
)).register("skill_stat_flash")
IntVariable(1230, Variable.Type.VARP).register("level_up_details")
IntVariable(965, Variable.Type.VARP).register("skill_guide")

InterfaceOpened where { name == "stats" } then {
    player.sendVar("skill_stat_flash")
    values().forEach {
        player.send(SkillLevelMessage(it.ordinal, 99, 14000000))
    }
}

InterfaceInteraction where { name == "stats" && option == "View" } then {
    val skill = valueOf(component.toUpperCase())
    val menuIndex = menu.indexOf(skill) + 1

    if(player.hasVar("skill_stat_flash", skill)) {
        val extra = 0//0 - normal, 2 - combat milestone, 4 - total milestone
        player.setVar("level_up_details", menuIndex * 8 + extra)
        player.open("skill_level_details")
        player.removeVar("skill_stat_flash", skill)
    } else {
        player.setVar("skill_guide", menuIndex)
        player.open("skill_guide")
    }
}