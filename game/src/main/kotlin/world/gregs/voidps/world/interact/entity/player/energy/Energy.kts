import kotlinx.coroutines.Job
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.Maths
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

on<EffectStart>({ effect == "energy" }) { player: Player ->
    player["energy_tick_job"] = delay(player, 1, loop = true) {
        val energy = player["energy", MAX_RUN_ENERGY]
        val movement = player.getVar("movement", "walk")
        val change = when {
            player.movement.moving && movement == "run" -> getDrainAmount(player)
            energy < MAX_RUN_ENERGY -> getRestoreAmount(player)
            else -> 0
        }
        if (change != 0) {
            player.runEnergy = energy + change
            walkWhenOutOfEnergy(player, player.runEnergy)
        }
    }
}

on<EffectStop>({ effect == "energy" }) { player: Player ->
    player.remove<Job>("energy_tick_job")?.cancel()
}

fun getDrainAmount(player: Player): Int {
    val weight = player["weight", 0].coerceIn(0, 64)
    var decrement = 67 + ((67 * weight) / 64)
    if (player.hasEffect("hamstring")) {
        decrement *= 4
    }
    return -decrement
}

fun getRestoreAmount(player: Player): Int {
    val agility = player.levels.get(Skill.Agility)
    // Approximations based on wiki
    return when (player.getVar("movement", "walk")) {
        "rest" -> Maths.interpolate(agility, 168, 310, 1, 99)
        "music" -> Maths.interpolate(agility, 240, 400, 1, 99)
        else -> Maths.interpolate(agility, 27, 157, 1, 99)
    }
}

fun walkWhenOutOfEnergy(player: Player, energy: Int) {
    if (energy == 0) {
        player.setVar("movement", "walk")
        player.running = false
    }
}