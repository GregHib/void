package content.skill.constitution.drink

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill

consume("karamjan_rum") { player ->
    player.levels.boost(Skill.Strength, 5)
    player.levels.drain(Skill.Attack, 4)
}

consume("vodka", "gin", "brandy", "whisky") { player ->
    player.levels.boost(Skill.Strength, 1, 0.05)
    player.levels.drain(Skill.Attack, 3, 0.02)
}

consume("bottle_of_wine") { player ->
    player.levels.drain(Skill.Attack, 3)
}

consume("braindeath_rum") { player ->
    player.levels.boost(Skill.Strength, 3)
    player.levels.boost(Skill.Mining, 1)
    player.levels.drain(Skill.Defence, multiplier = 0.10)
    player.levels.drain(Skill.Attack, multiplier = 0.05)
    player.levels.drain(Skill.Prayer, multiplier = 0.05)
    player.levels.drain(Skill.Ranged, multiplier = 0.05)
    player.levels.drain(Skill.Magic, multiplier = 0.05)
    player.levels.drain(Skill.Agility, multiplier = 0.05)
    player.levels.drain(Skill.Herblore, multiplier = 0.05)
}
