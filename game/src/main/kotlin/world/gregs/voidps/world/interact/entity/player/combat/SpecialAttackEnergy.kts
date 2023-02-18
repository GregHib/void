package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import kotlin.math.min

on<Registered>({ it.specialAttackEnergy < MAX_SPECIAL_ATTACK }) { player: Player ->
    player.softTimers.start("restore_special_energy")
}

on<TimerStart>({ timer == "restore_special_energy" }) { _: Player ->
    interval = 50
}

val half = MAX_SPECIAL_ATTACK / 2
val tenth = MAX_SPECIAL_ATTACK / 10

on<TimerTick>({ timer == "restore_special_energy" }) { player: Player ->
    val energy = player.specialAttackEnergy
    if (energy >= MAX_SPECIAL_ATTACK) {
        return@on cancel()
    }
    val restore = min(tenth, MAX_SPECIAL_ATTACK - energy)
    player.specialAttackEnergy += restore
    if (player.specialAttackEnergy.rem(half) == 0) {
        player.message("Your special attack energy is now ${if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) 100 else 50}%.")
    }
}