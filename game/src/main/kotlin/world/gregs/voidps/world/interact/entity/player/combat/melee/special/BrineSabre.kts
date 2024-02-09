package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.specialAttack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("brine_sabre") { player: Player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        player.specialAttack = false
    }
}

specialAttackSwing("brine_sabre") { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("liquify")
    player.setGraphic("liquify")
    player.hit(target)
    delay = 4
}