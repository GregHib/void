package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.variable.ListVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.addVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Leveled
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.sound.playMusicEffect

ListVariable(4757, Variable.Type.VARBIT, false, values = listOf(
    -1, Attack, Strength, Range, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting, Mining,
    Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering
)).register("level_up_icon")

on<Leveled>({ to > from }) { player: Player ->
    player.dialogue {
        val unlock =  when (skill) {
            Agility -> false
            Construction -> to.rem(10) == 0
            Constitution, Strength -> to >= 50
            Hunter -> to.rem(2) == 0
            else -> true// TODO has unlocked something
        }
        player.playMusicEffect("level_up_${skill.name.toLowerCase()}${if (unlock) "_unlock" else ""}", 0.5)
        player.setGraphic("level_up")
        player.addVar("skill_stat_flash", skill)
        levelUp("""
            Congratulations! You've just advanced a${if (skill.name.startsWith("A")) "n" else ""} ${skill.name} level!
            You have now reached level ${to}!
        """, skill)
    }
}