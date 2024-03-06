package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

specialAttack("energy_drain") { player ->
    player.setAnimation(id)
    if (player.hit(target) != -1) {
        target.setGraphic(id)
        if (target is Player) {
            val tenPercent = (target.runEnergy / 100) * 10
            if (tenPercent > 0) {
                target.runEnergy -= tenPercent
                player.runEnergy += tenPercent
                target.message("You feel drained!")
            }
        }
    }
}