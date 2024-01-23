package world.gregs.voidps.world.interact.dialogue.type

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.exp.GrantExp
import world.gregs.voidps.engine.entity.character.player.skill.level.MaxLevelChanged
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.sound.playJingle

on<GrantExp> { player: Player ->
    val previousLevel = Experience.level(skill, from)
    val currentLevel = Experience.level(skill, to)
    if (currentLevel != previousLevel) {
        player.levels.restore(skill, 1)
        player.events.emit(MaxLevelChanged(skill, previousLevel, currentLevel))
    }
}

on<MaxLevelChanged>({ to > from && !it["skip_level_up", false] }) { player: Player ->
    player.weakQueue(name = "level_up") {
        val unlock = when (skill) {
            Agility -> false
            Construction -> to.rem(10) == 0
            Constitution, Strength -> to >= 50
            Hunter -> to.rem(2) == 0
            else -> true// TODO has unlocked something
        }
        player.playJingle("level_up_${skill.name.lowercase()}${if (unlock) "_unlock" else ""}", 0.5)
        player.setGraphic("level_up")
        player.addVarbit("skill_stat_flash", skill.name.toSnakeCase())
        levelUp(skill, """
            Congratulations! You've just advanced${skill.name.an()} ${skill.name} level!
            You have now reached level ${to}!
        """)
    }
}

on<CombatHit>({ !(it.menu ?: it.dialogue).isNullOrBlank() }) { player: Player ->
    player.closeInterfaces()
}
