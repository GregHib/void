package content.entity.player.dialogue.type

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skill.*
import world.gregs.voidps.engine.entity.character.player.skill.Skills
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.suspend.ContinueSuspension

private const val LEVEL_UP_INTERFACE_ID = "dialogue_level_up"

suspend fun Player.levelUp(skill: Skill, text: String) {
    levelUp(this, skill, text)
    ContinueSuspension.get(this)
    close(LEVEL_UP_INTERFACE_ID)
}

fun levelUp(player: Player, skill: Skill, text: String) {
    val lines = text.trimIndent().lines()
    player["level_up_icon"] = skill.name
    player.sendVariable("level_up_icon")
    check(player.open(LEVEL_UP_INTERFACE_ID)) { "Unable to open level up interface for $player" }
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(LEVEL_UP_INTERFACE_ID, "line${index + 1}", line)
    }
}

class LevelUp : Script {

    init {
        experience { skill, from, to ->
            val previousLevel = Experience.level(skill, from / 10.0)
            val currentLevel = Experience.level(skill, to / 10.0)
            if (currentLevel != previousLevel) {
                levels.restore(skill, currentLevel - previousLevel)
                Skills.maxChanged(this, skill, previousLevel, currentLevel)
            }
        }

        maxLevelChanged { skill, from, to ->
            if (from >= to) {
                return@maxLevelChanged
            }
            if (get("skip_level_up", false)) {
                return@maxLevelChanged
            }
            AuditLog.event(this, "level_up", skill.name, to)
            val unlock = when (skill) {
                Agility -> false
                Construction -> to.rem(10) == 0
                Constitution, Strength -> to >= 50
                Hunter -> to.rem(2) == 0
                else -> true // TODO has unlocked something
            }
            jingle("level_up_${skill.name.lowercase()}${if (unlock) "_unlock" else ""}", 0.5)
            addVarbit("skill_stat_flash", skill.name.lowercase())
            val level = if (skill == Constitution) to / 10 else to
            levelUp(
                this,
                skill,
                """
                Congratulations! You've just advanced${skill.name.an()} ${skill.name} level!
                You have now reached level $level!
            """,
            )
        }

        combatDamage {
            if (!(menu ?: dialogue).isNullOrBlank()) {
                closeInterfaces()
            }
        }
    }
}
