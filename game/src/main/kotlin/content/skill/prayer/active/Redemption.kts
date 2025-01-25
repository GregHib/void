package content.skill.prayer.active

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import content.skill.prayer.praying

levelChange(Skill.Constitution) { player ->
    if (to <= 0 || to >= player.levels.getMax(skill) / 10 || !player.praying("redemption")) {
        return@levelChange
    }
    player.levels.set(Skill.Prayer, 0)
    val health = (player.levels.getMax(Skill.Prayer) * 2.5).toInt()
    player.levels.restore(Skill.Constitution, health)
    player.gfx("redemption")
}