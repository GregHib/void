package content.skill.prayer.active

import content.skill.prayer.praying
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Redemption : Script {
    init {
        levelChanged(Skill.Constitution) { skill, from, to ->
            if (to <= 0 || to >= levels.getMax(skill) / 10 || !praying("redemption")) {
                return@levelChanged
            }
            levels.set(Skill.Prayer, 0)
            val health = (levels.getMax(Skill.Prayer) * 2.5).toInt()
            levels.restore(Skill.Constitution, health)
            gfx("redemption")
        }
    }
}
