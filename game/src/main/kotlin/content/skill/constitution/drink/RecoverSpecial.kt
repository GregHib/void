package content.skill.constitution.drink

import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.timer.*

class RecoverSpecial : Script {

    init {
        consumable("recover_special*") {
            if (specialAttackEnergy == MAX_SPECIAL_ATTACK) {
                message("Drinking this would have no effect.")
                false
            } else if (softTimers.contains("recover_special")) {
                message("You may only use this pot once every 30 seconds.")
                false
            } else {
                true
            }
        }

        timerStart("recover_special") { 10 }
        timerTick("recover_special") { if (dec("recover_special_delay") <= 0) Timer.CANCEL else Timer.CONTINUE }
        timerStop("recover_special") { clear("recover_special_delay") }
    }
}
