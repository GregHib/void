package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.variable.BitwiseVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.world.activity.skill.Skill.*

BitwiseVariable(4757, Variable.Type.VARBIT, false, values = listOf(
    ATTACK, STRENGTH, RANGE, MAGIC, DEFENCE, CONSTITUTION, PRAYER, AGILITY, HERBLORE, THIEVING, CRAFTING, RUNECRAFTING, MINING,
    SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, FLETCHING, SLAYER, FARMING, CONSTRUCTION, HUNTER, SUMMONING, DUNGEONEERING
)).register("level_up_icon")