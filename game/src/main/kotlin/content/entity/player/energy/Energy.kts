package content.entity.player.energy

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerTick

playerSpawn { player ->
    if (player.runEnergy < MAX_RUN_ENERGY) {
        player.softTimers.start("energy_restore")
    }
}

timerTick("energy_restore") { player ->
    if (player.runEnergy >= MAX_RUN_ENERGY) {
        cancel()
        return@timerTick
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

move({ it.visuals.runStep != -1 }) { player ->
    if (player["last_energy_drain", -1] == GameLoop.tick || !Settings["players.energy.drain", true]) {
        return@move
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