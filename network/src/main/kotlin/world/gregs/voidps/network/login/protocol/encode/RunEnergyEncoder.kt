package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.RUN_ENERGY

/**
 * Sends run energy
 * @param energy The current energy value
 */
fun Client.sendRunEnergy(energy: Int) {
    send(RUN_ENERGY) {
        writeByte(energy)
    }
}