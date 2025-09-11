package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.PLAYER_WEIGHT
import world.gregs.voidps.network.login.protocol.writeShort

/**
 * Updates player weight for equipment screen
 */
fun Client.weight(
    weight: Int,
) = send(PLAYER_WEIGHT) {
    writeShort(weight)
}
