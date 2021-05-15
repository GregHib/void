package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get

const val MAX_ENERGY = 10000

fun Player.energyPercent() = (this["energy", MAX_ENERGY] / MAX_ENERGY.toDouble() * 100).toInt()