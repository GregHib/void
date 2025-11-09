package content.skill.melee.weapon.special

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class BrineSabre : Script {

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
