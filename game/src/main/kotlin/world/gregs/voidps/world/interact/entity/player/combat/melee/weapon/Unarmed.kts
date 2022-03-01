package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit

on<CombatSwing>({ !swung() }, Priority.LOWEST) { player: Player ->
    player.setAnimation(if (player.attackType == "kick") "player_kick" else "player_punch")
    player.hit(target, null)
    delay = 4
}

on<CombatHit>({ !blocked }, Priority.LOWER) { player: Player ->
    player.setAnimation("block")
    blocked = true
}