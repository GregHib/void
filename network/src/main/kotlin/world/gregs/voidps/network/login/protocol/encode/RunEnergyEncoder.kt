package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.RUN_ENERGY
import world.gregs.voidps.network.login.protocol.writeByte

/**
 * Sends run energy
 * @param energy The current energy value
 */
fun Client.sendRunEnergy(energy: Int) = send(RUN_ENERGY) {
    writeByte(energy)
}
