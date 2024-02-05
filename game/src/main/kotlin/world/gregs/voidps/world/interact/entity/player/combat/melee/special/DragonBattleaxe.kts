package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.specialAttack
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("*dragon_battleaxe") { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@specialAttack
    }
    player.setAnimation("rampage")
    player.setGraphic("rampage")
    player.forceChat = "Raarrrrrgggggghhhhhhh!"
    player.levels.drain(Skill.Attack, multiplier = 0.10)
    player.levels.drain(Skill.Defence, multiplier = 0.10)
    player.levels.drain(Skill.Magic, multiplier = 0.10)
    player.levels.drain(Skill.Ranged, multiplier = 0.10)
    player.levels.boost(Skill.Strength, amount = 5, multiplier = 0.15)
    player.specialAttack = false
}