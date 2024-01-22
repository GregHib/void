package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol.RUN_ENERGY
import world.gregs.voidps.network.client.Client

/**
 * Sends run energy
 * @param energy The current energy value
 */
fun Client.sendRunEnergy(energy: Int) = send(RUN_ENERGY) {
    writeByte(energy)
}