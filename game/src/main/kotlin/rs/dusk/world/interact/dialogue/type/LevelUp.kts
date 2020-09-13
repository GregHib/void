package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.variable.BitwiseVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.entity.character.player.skill.Skill.*

BitwiseVariable(4757, Variable.Type.VARBIT, false, values = listOf(
    Attack, Strength, Range, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting, Mining,
    Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering
)).register("level_up_icon")