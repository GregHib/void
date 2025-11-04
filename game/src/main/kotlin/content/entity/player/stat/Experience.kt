package content.entity.player.stat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.encode.skillLevel

class Experience : Script {

    init {
        playerSpawn {
            sendVariable("xp_counter")
        }

        levelChanged { skill, _, to ->
            if (skill == Skill.Constitution) {
                val exp = experience.get(skill)
                client?.skillLevel(skill.ordinal, to / 10, exp.toInt())
                set("life_points", levels.get(Skill.Constitution))
            } else {
                val exp = experience.get(skill)
                client?.skillLevel(skill.ordinal, to, exp.toInt())
            }
        }

        interfaceOption("Reset XP Total", "toplevel*:xp_orb") {
            set("xp_counter", 0.0)
        }

        experience { _, from, to ->
            val current = get("xp_counter", 0.0)
            val increase = to - from
            set("xp_counter", current + increase)
            set("lifetime_xp", get("lifetime_xp", 0.0) + increase)
        }

        experience { skill, _, to ->
            val level = levels.get(skill)
            client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to.toInt())
        }
    }
}
