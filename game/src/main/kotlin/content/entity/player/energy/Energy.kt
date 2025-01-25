package content.entity.player.energy

import world.gregs.voidps.engine.client.sendRunEnergy
import world.gregs.voidps.engine.entity.character.player.Player

const val MAX_RUN_ENERGY = 10000

fun Player.energyPercent() = (runEnergy / MAX_RUN_ENERGY.toDouble() * 100).toInt()

var Player.runEnergy: Int
    get() = this["energy", MAX_RUN_ENERGY]
    set(value) {
        this["energy"] = value.coerceIn(0, MAX_RUN_ENERGY)
        softTimers.startIfAbsent("energy_restore")
        sendRunEnergy(energyPercent())
    }