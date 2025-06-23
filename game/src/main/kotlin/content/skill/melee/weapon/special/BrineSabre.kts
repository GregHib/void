package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackPrepare
import world.gregs.voidps.engine.client.message

specialAttackPrepare("brine_sabre") { player ->
    if (player.tile.region.id != 11924) {
        player.message("You can only use this special attack under water.")
        cancel()
    }
}
