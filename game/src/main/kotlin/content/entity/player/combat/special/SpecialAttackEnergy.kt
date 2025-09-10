package content.entity.player.combat.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import kotlin.math.min
import world.gregs.voidps.engine.event.Script
@Script
class SpecialAttackEnergy {

    val half = MAX_SPECIAL_ATTACK / 2
    val tenth = MAX_SPECIAL_ATTACK / 10
    
    init {
        playerSpawn { player ->
            if (player.specialAttackEnergy < MAX_SPECIAL_ATTACK) {
                player.softTimers.start("restore_special_energy")
            }
        }

        timerStart("restore_special_energy") {
            interval = 50
        }

        timerTick("restore_special_energy") { player ->
            val energy = player.specialAttackEnergy
            if (energy >= MAX_SPECIAL_ATTACK) {
                cancel()
                return@timerTick
            }
            val restore = min(tenth, MAX_SPECIAL_ATTACK - energy)
            player.specialAttackEnergy += restore
            if (player.specialAttackEnergy.rem(half) == 0) {
                player.message("Your special attack energy is now ${if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) 100 else 50}%.")
            }
        }

    }

}
