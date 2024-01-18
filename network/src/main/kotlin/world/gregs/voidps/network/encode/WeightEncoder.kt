package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol.PLAYER_WEIGHT
import world.gregs.voidps.network.client.Client

/**
 * Updates player weight for equipment screen
 */
fun Client.weight(
    weight: Int
) = send(PLAYER_WEIGHT) {
    writeShort(weight)
}