package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.stopSoftTimer
import kotlin.math.min

on<TimerTick>({ timer == "restore_special_energy" }) { player: Player ->
    val energy = player.specialAttackEnergy
    if (energy >= MAX_SPECIAL_ATTACK) {
        player.stopSoftTimer(timer)
        return@on
    }
    val restore = min(MAX_SPECIAL_ATTACK / 10, MAX_SPECIAL_ATTACK - energy)
    player.specialAttackEnergy += restore
    if (player.specialAttackEnergy.rem(MAX_SPECIAL_ATTACK / 2) == 0) {
        player.message("Your special attack energy is now ${if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) 100 else 50}%.")
    }
}