package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.stopTimer
import world.gregs.voidps.engine.timer.timer

on<EffectStart>({ effect == "energy" }) { player: Player ->
    player.timer("energy_restore", 1) {
        if (player.runEnergy < MAX_RUN_ENERGY) {
            player.runEnergy += getRestoreAmount(player)
        }
    }
}

fun getRestoreAmount(player: Player): Int {
    val agility = player.levels.get(Skill.Agility)
    // Approximations based on wiki
    return when (player.getVar("movement", "walk")) {
        "rest" -> Interpolation.interpolate(agility, 168, 310, 1, 99)
        "music" -> Interpolation.interpolate(agility, 240, 400, 1, 99)
        else -> Interpolation.interpolate(agility, 27, 157, 1, 99)
    }
}

on<EffectStop>({ effect == "energy" }) { player: Player ->
    player.stopTimer("energy_restore")
}

on<Moved>({ it.visuals.runStep != -1 && it.hasEffect("energy") }) { player: Player ->
    if (player["last_energy_drain", -1L] == GameLoop.tick) {
        return@on
    }
    player["last_energy_drain"] = GameLoop.tick
    if (player.visuals.runStep != -1) {
        player.runEnergy -= getDrainAmount(player)
        walkWhenOutOfEnergy(player)
    }
}

fun getDrainAmount(player: Player): Int {
    val weight = player["weight", 0].coerceIn(0, 64)
    var decrement = 67 + ((67 * weight) / 64)
    if (player.hasEffect("hamstring")) {
        decrement *= 4
    }
    return decrement
}

fun walkWhenOutOfEnergy(player: Player) {
    if (player.runEnergy == 0) {
        player.setVar("movement", "walk")
        player.running = false
    }
}