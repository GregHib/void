package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Protocol.RUN_ENERGY

/**
 * Sends run energy
 * @param energy The current energy value
 */
fun Player.sendRunEnergy(energy: Int) = client?.send(RUN_ENERGY) {
    writeByte(energy)
}