package world.gregs.voidps.world.interact.dialogue.type

import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension

private const val EXPERIENCE_SKILL_LAMP = "skill_stat_advance"

suspend fun CharacterContext.skillLamp(): Skill {
    check(player.open(EXPERIENCE_SKILL_LAMP)) { "Unable to open skill lamp dialogue for $player" }
    val result = StringSuspension()
    player.close(EXPERIENCE_SKILL_LAMP)
    return Skill.valueOf(result.toPascalCase())
}