package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

consume({ item.id == "cup_of_tea" }) { player: Player ->
    player.levels.boost(Skill.Attack, 3)
}

consume({ item.id.startsWith("guthix_rest") }) { player: Player ->
    if (player.poisoned) {
        player["poison_damage"] = player["poison_damage", 0] - 10
    }
    player.runEnergy += (player.runEnergy / 100) * 5
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random()
    player.levels.boost(Skill.Constitution, amount, maximum = 50)
    cancel()
}

consume({ item.id == "nettle_tea" }) { player: Player ->
    player.runEnergy = (player.runEnergy / 100) * 5
}