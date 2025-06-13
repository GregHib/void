package content.entity.player.dialogue.type

import content.entity.combat.hit.combatDamage
import content.entity.sound.jingle
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.exp.experience
import world.gregs.voidps.engine.entity.character.player.skill.level.MaxLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.level.maxLevelChange

experience { player ->
    val previousLevel = Experience.level(skill, from)
    val currentLevel = Experience.level(skill, to)
    if (currentLevel != previousLevel) {
        player.levels.restore(skill, currentLevel - previousLevel)
        player.emit(MaxLevelChanged(skill, previousLevel, currentLevel))
    }
}

maxLevelChange { player ->
    if (from >= to) {
        return@maxLevelChange
    }
    if (player["skip_level_up", false]) {
        return@maxLevelChange
    }
    val unlock = when (skill) {
        Agility -> false
        Construction -> to.rem(10) == 0
        Constitution, Strength -> to >= 50
        Hunter -> to.rem(2) == 0
        else -> true // TODO has unlocked something
    }
    player.jingle("level_up_${skill.name.lowercase()}${if (unlock) "_unlock" else ""}", 0.5)
    player.addVarbit("skill_stat_flash", skill.name.lowercase())
    val level = if (skill == Constitution) to / 10 else to
    levelUp(
        player,
        skill,
        """
        Congratulations! You've just advanced${skill.name.an()} ${skill.name} level!
        You have now reached level $level!
    """,
    )
}

combatDamage { player ->
    if (!(player.menu ?: player.dialogue).isNullOrBlank()) {
        player.closeInterfaces()
    }
}
