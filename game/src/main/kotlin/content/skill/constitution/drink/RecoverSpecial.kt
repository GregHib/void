package content.skill.constitution.drink

import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.skill.constitution.canConsume
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.event.Script
@Script
class RecoverSpecial {

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

        timerStart("recover_special") {
            interval = 10
        }

        timerTick("recover_special") { player ->
            if (player.dec("recover_special_delay") <= 0) {
                cancel()
                return@timerTick
            }
        }

        timerStop("recover_special") { player ->
            player.clear("recover_special_delay")
        }

    }

}
