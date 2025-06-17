package content.skill.constitution.drink

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill

consume("bandits_brew") { player ->
    player.levels.boost(Skill.Thieving, 1)
    player.levels.boost(Skill.Attack, 1)
    player.levels.drain(Skill.Strength, 3, 0.06)
    player.levels.drain(Skill.Defence, 3, 0.06)
}

consume("beer") { player ->
    player.levels.boost(Skill.Strength, 1, 0.02)
    player.levels.drain(Skill.Attack, 1, 0.06)
    player["dishwater_task"] = true
}

consume("keg_of_beer*") { player ->
    player.levels.boost(Skill.Strength, 2, 0.10)
    player.levels.drain(Skill.Attack, 5, 0.50)
    player.timers.start("drunk") // TODO screen wobble until teleport
}

consume("grog") { player ->
    player.levels.boost(Skill.Strength, 3)
    player.levels.drain(Skill.Attack, 6)
}
