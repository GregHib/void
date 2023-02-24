package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerTick

on<Registered>({ it.runEnergy < MAX_RUN_ENERGY }) { player: Player ->
    player.softTimers.start("energy_restore")
}

on<TimerTick>({ timer == "energy_restore" }) { player: Player ->
    if (player.runEnergy >= MAX_RUN_ENERGY) {
        return@on cancel()
    }
    player.runEnergy += getRestoreAmount(player)
}

fun getRestoreAmount(player: Player): Int {
    val agility = player.levels.get(Skill.Agility)
    // Approximations based on wiki
    return when (player["movement", "walk"]) {
        "rest" -> Interpolation.interpolate(agility, 168, 310, 1, 99)
        "music" -> Interpolation.interpolate(agility, 240, 400, 1, 99)
        else -> Interpolation.interpolate(agility, 27, 157, 1, 99)
    }
}

on<Moved>({ it.visuals.runStep != -1 }) { player: Player ->
    if (player["last_energy_drain", -1] == GameLoop.tick) {
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
    if (player.hasClock("hamstring")) {
        decrement *= 4
    }
    return decrement
}

fun walkWhenOutOfEnergy(player: Player) {
    if (player.runEnergy == 0) {
        player["movement"] = "walk"
        player.running = false
    }
}