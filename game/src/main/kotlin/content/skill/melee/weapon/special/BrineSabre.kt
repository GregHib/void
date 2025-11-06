package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class BrineSabre : Script, SpecialAttack {

    init {
        specialAttackPrepare("brine_sabre") {
            if (tile.region.id != 11924) {
                message("You can only use this special attack under water.")
                false
            } else {
                true
            }
        }
    }
}
