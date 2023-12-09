package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.combat.consume.Consume

on<Consume>({ item.id == "bandits_brew" }) { player: Player ->
    player.levels.boost(Skill.Thieving, 1)
    player.levels.boost(Skill.Attack, 1)
    player.levels.drain(Skill.Strength, 3, 0.06)
    player.levels.drain(Skill.Defence, 3, 0.06)
}

on<Consume>({ item.id == "beer" }) { player: Player ->
    player.levels.boost(Skill.Strength, 1, 0.02)
    player.levels.drain(Skill.Attack, 1, 0.06)
}

on<Consume>({ item.id.startsWith("keg_of_beer") }) { player: Player ->
    player.levels.boost(Skill.Strength, 2, 0.10)
    player.levels.drain(Skill.Attack, 5, 0.50)
    player.timers.start("drunk")// TODO screen wobble until teleport
}

on<Consume>({ item.id == "grog" }) { player: Player ->
    player.levels.boost(Skill.Strength, 3)
    player.levels.drain(Skill.Attack, 6)
}