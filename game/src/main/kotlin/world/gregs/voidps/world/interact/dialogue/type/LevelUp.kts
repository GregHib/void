package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.variable.BitwiseVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*

BitwiseVariable(4757, Variable.Type.VARBIT, false, values = listOf(
    Attack, Strength, Range, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting, Mining,
    Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering
)).register("level_up_icon")