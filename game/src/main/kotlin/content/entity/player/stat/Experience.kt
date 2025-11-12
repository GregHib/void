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
            val exp = experience.direct(skill) / 10
            if (skill == Skill.Constitution) {
                client?.skillLevel(skill.ordinal, to / 10, exp)
                set("life_points", levels.get(Skill.Constitution))
            } else {
                client?.skillLevel(skill.ordinal, to, exp)
            }
        }

        interfaceOption("Reset XP Total", "toplevel*:xp_orb") {
            set("xp_counter", 0)
        }

        experience { _, from, to ->
            val increase = (to - from)
            inc("xp_counter", increase)
            set("lifetime_xp", get("lifetime_xp", 0L) + increase.toLong())
        }

        experience { skill, _, to ->
            val level = levels.get(skill)
            client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to / 10)
        }
    }
}
