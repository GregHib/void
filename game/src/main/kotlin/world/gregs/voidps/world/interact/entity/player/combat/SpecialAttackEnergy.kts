import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Job
import world.gregs.voidps.engine.tick.timer
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import kotlin.math.min

on<EffectStart>({ effect == "restore_special_energy" }) { player: Player ->
    player["spec_energy_job"] = player.timer(50, loop = true) {
        val energy = player.specialAttackEnergy
        if (energy >= MAX_SPECIAL_ATTACK) {
            player.stop(effect)
            return@timer
        }
        val restore = min(MAX_SPECIAL_ATTACK / 10, MAX_SPECIAL_ATTACK - energy)
        player.specialAttackEnergy += restore
        if (player.specialAttackEnergy.rem(MAX_SPECIAL_ATTACK / 2) == 0) {
            player.message("Your special attack energy is now ${if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) 100 else 50}%.")
        }
    }
}

on<EffectStop>({ effect == "restore_special_energy" }) { player: Player ->
    player.remove<Job>("spec_energy_job")?.cancel()
}