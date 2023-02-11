package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Consume>({ item.id == "wizard_blizzard" }) { player: Player ->
    player.levels.boost(Skill.Strength, 6)
    player.levels.drain(Skill.Attack, 4)
}

on<Consume>({ item.id == "short_green_guy" }) { player: Player ->
    player.levels.boost(Skill.Strength, 4)
    player.levels.drain(Skill.Attack, 3)
}

on<Consume>({ item.id == "drunk_dragon" }) { player: Player ->
    player.levels.boost(Skill.Strength, 5)
    player.levels.drain(Skill.Attack, 4)
}

on<Consume>({ item.id == "chocolate_saturday" }) { player: Player ->
    player.levels.boost(Skill.Strength, 7)
    player.levels.drain(Skill.Attack, 4)
}

on<Consume>({ item.id == "blurberry_special" }) { player: Player ->
    player.levels.boost(Skill.Strength, 6)
    player.levels.drain(Skill.Attack, 4)
}