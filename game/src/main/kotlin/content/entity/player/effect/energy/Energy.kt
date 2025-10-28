package content.entity.player.effect.energy

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.sendRunEnergy
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile

const val MAX_RUN_ENERGY = 10000

fun Player.energyPercent() = (runEnergy / MAX_RUN_ENERGY.toDouble() * 100).toInt()

var Player.runEnergy: Int
    get() = this["energy", MAX_RUN_ENERGY]
    set(value) {
        this["energy"] = value.coerceIn(0, MAX_RUN_ENERGY)
        softTimers.startIfAbsent("energy_restore")
        sendRunEnergy(energyPercent())
    }

@Script
class Energy : Api {

    init {
        playerSpawn { player ->
            if (player.runEnergy < MAX_RUN_ENERGY) {
                player.softTimers.start("energy_restore")
            }
        }
    }

    override fun move(player: Player, from: Tile, to: Tile) {
        if (player.visuals.runStep == -1 || player["last_energy_drain", -1] == GameLoop.tick || !Settings["players.energy.drain", true]) {
            return
        }
        player["last_energy_drain"] = GameLoop.tick
        if (player.visuals.runStep != -1) {
            player.runEnergy -= getDrainAmount(player)
            walkWhenOutOfEnergy(player)
        }
    }

    @Timer("energy_restore")
    override fun tick(player: Player, timer: String): Int {
        if (player.runEnergy >= MAX_RUN_ENERGY) {
            return Timer.CANCEL
        }
        player.runEnergy += getRestoreAmount(player)
        return Timer.CONTINUE
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
}
