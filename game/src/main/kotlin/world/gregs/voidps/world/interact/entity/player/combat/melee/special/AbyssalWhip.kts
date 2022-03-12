package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

fun isWhip(item: Item?) = item != null && item.id.startsWith("abyssal_whip")

on<CombatSwing>({ !swung() && isWhip(it.weapon) }, Priority.LOW) { player: Player ->
    if (player.specialAttack && !drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("whip_${player.attackType}")
    if (player.hit(target) != -1 && player.specialAttack) {
        if (target is Player) {
            val tenPercent = (target.runEnergy / 100) * 10
            if (tenPercent > 0) {
                target.runEnergy -= tenPercent
                player.runEnergy += tenPercent
                target.message("You feel drained!")
            }
        }
        target.setGraphic("energy_drain")
    }
    delay = 4
}

on<CombatHit>({ !blocked && isWhip(it.weapon) }) { player: Player ->
    player.setAnimation("whip_block")
    blocked = true
}

// Special attack

specialAccuracyMultiplier(1.25, ::isWhip)