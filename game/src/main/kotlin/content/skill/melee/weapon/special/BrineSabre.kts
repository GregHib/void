package content.skill.melee.weapon.special

import world.gregs.voidps.engine.client.message
import content.entity.player.combat.special.specialAttackPrepare

specialAttackPrepare("brine_sabre") { player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        cancel()
    }
}