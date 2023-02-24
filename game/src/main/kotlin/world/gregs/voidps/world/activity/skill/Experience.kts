package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.GrantExp
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.skillLevel

on<Registered> { player: Player ->
    player.sendVariable("xp_counter")
}

on<InterfaceOption>({ id == it.gameFrame.name && component == "xp_orb" && option == "Reset XP Total" }) { player: Player ->
    player.setVar("xp_counter", 0.0)
}

on<GrantExp> { player: Player ->
    val current = player.getVar<Double>("xp_counter")
    val increase = to - from
    player.setVar("xp_counter", current + increase)
    player["lifetime_xp"] = player["lifetime_xp", 0.0] + increase
}

on<GrantExp> { player: Player ->
    val level = player.levels.get(skill)
    player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to.toInt())
}

on<CurrentLevelChanged>({ skill != Skill.Constitution }) { player: Player ->
    val exp = player.experience.get(skill)
    player.client?.skillLevel(skill.ordinal, to, exp.toInt())
}

on<CurrentLevelChanged>({ skill == Skill.Constitution }) { player: Player ->
    val exp = player.experience.get(skill)
    player.client?.skillLevel(skill.ordinal, to / 10, exp.toInt())
    player.setVar("life_points", player.levels.get(Skill.Constitution))
}