package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.experience
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.network.encode.skillLevel

playerSpawn { player: Player ->
    player.sendVariable("xp_counter")
}

interfaceOption("Reset XP Total", "xp_orb", "toplevel*") {
    player["xp_counter"] = 0.0
}

experience { player: Player ->
    val current = player["xp_counter", 0.0]
    val increase = to - from
    player["xp_counter"] = current + increase
    player["lifetime_xp"] = player["lifetime_xp", 0.0] + increase
}

experience { player: Player ->
    val level = player.levels.get(skill)
    player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to.toInt())
}

levelChange { player: Player ->
    if (skill == Skill.Constitution) {
        val exp = player.experience.get(skill)
        player.client?.skillLevel(skill.ordinal, to / 10, exp.toInt())
        player["life_points"] = player.levels.get(Skill.Constitution)
    } else {
        val exp = player.experience.get(skill)
        player.client?.skillLevel(skill.ordinal, to, exp.toInt())
    }
}