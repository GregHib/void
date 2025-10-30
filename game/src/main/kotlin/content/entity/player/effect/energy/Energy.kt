package content.entity.player.effect.energy

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendRunEnergy
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.timer.Timer

const val MAX_RUN_ENERGY = 10000

fun Player.energyPercent() = (runEnergy / MAX_RUN_ENERGY.toDouble() * 100).toInt()

var Player.runEnergy: Int
    get() = this["energy", MAX_RUN_ENERGY]
    set(value) {
        this["energy"] = value.coerceIn(0, MAX_RUN_ENERGY)
        softTimers.startIfAbsent("energy_restore")
        sendRunEnergy(energyPercent())
    }

class Energy : Script {

    init {
        playerSpawn {
            if (runEnergy < MAX_RUN_ENERGY) {
                softTimers.start("energy_restore")
            }
        }

        moved { player, _ ->
            if (player.visuals.runStep == -1 || player["last_energy_drain", -1] == GameLoop.tick || !Settings["players.energy.drain", true]) {
                return@moved
            }
            player["last_energy_drain"] = GameLoop.tick
            if (player.visuals.runStep != -1) {
                player.runEnergy -= getDrainAmount(player)
                walkWhenOutOfEnergy(player)
            }
        }

        timerTick("energy_restore") {
            if (runEnergy >= MAX_RUN_ENERGY) {
                return@timerTick Timer.CANCEL
            }
            runEnergy += getRestoreAmount(this)
            return@timerTick Timer.CONTINUE
        }
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
