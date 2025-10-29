package content.skill.constitution.drink

import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.skill.constitution.canConsume
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*

@Script
class RecoverSpecial : Api {

    init {
        canConsume("recover_special*") { player ->
            if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) {
                player.message("Drinking this would have no effect.")
                cancel()
            } else if (player.softTimers.contains("recover_special")) {
                player.message("You may only use this pot once every 30 seconds.")
                cancel()
            }
        }

        timerStart("recover_special") { 10 }
        timerTick("recover_special") { if (dec("recover_special_delay") <= 0) Timer.CANCEL else Timer.CONTINUE }
        timerStop("recover_special") { clear("recover_special_delay") }
    }
}
