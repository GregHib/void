package content.entity.player.combat.special

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.Key
import world.gregs.voidps.engine.timer.Timer
import kotlin.math.min

@Script
class SpecialAttackEnergy : Api {

    val half = MAX_SPECIAL_ATTACK / 2
    val tenth = MAX_SPECIAL_ATTACK / 10

    override fun spawn(player: Player) {
        if (player.specialAttackEnergy < MAX_SPECIAL_ATTACK) {
            player.softTimers.start("restore_special_energy")
        }
    }

    @Key("restore_special_energy")
    override fun start(player: Player, timer: String, restart: Boolean) = 50

    @Key("restore_special_energy")
    override fun tick(player: Player, timer: String): Int {
        val energy = player.specialAttackEnergy
        if (energy >= MAX_SPECIAL_ATTACK) {
            return Timer.CANCEL
        }
        val restore = min(tenth, MAX_SPECIAL_ATTACK - energy)
        player.specialAttackEnergy += restore
        if (player.specialAttackEnergy.rem(half) == 0) {
            player.message("Your special attack energy is now ${if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) 100 else 50}%.")
        }
        return Timer.CONTINUE
    }
}
