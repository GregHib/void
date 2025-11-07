package content.entity.player.dialogue.type

import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.StringSuspension

private const val EXPERIENCE_SKILL_LAMP = "skill_stat_advance"

suspend fun Player.skillLamp(): Skill {
    check(open(EXPERIENCE_SKILL_LAMP)) { "Unable to open skill lamp dialogue for $this" }
    val result = StringSuspension.get(this)
    close(EXPERIENCE_SKILL_LAMP)
    return Skill.valueOf(result.toPascalCase())
}
