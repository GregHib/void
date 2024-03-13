package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackPrepare

specialAttackPrepare("brine_sabre") { player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        cancel()
    }
}