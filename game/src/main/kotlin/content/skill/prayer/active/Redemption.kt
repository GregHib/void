package content.skill.prayer.active

import content.skill.prayer.praying
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class Redemption : Api {
    init {
        levelChanged(Skill.Constitution) { player, skill, from, to ->
            if (to <= 0 || to >= player.levels.getMax(skill) / 10 || !player.praying("redemption")) {
                return@levelChanged
            }
            player.levels.set(Skill.Prayer, 0)
            val health = (player.levels.getMax(Skill.Prayer) * 2.5).toInt()
            player.levels.restore(Skill.Constitution, health)
            player.gfx("redemption")
        }
    }
}
