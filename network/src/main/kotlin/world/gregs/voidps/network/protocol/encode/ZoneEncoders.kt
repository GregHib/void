package world.gregs.voidps.network.protocol.encode

import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.Protocol.CLEAR_ZONE
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteInverse

/**
 * @param xOffset The zone x coordinate relative to viewport
 * @param yOffset The zone y coordinate relative to viewport
 * @param level The zones level
 */
fun Client.clearZone(
    xOffset: Int,
    yOffset: Int,
    level: Int
) = send(CLEAR_ZONE) {
    writeByteAdd(level)
    writeByteInverse(yOffset)
    writeByteInverse(xOffset)
}

/**
 * @param xOffset The zone x coordinate relative to viewport
 * @param yOffset The zone y coordinate relative to viewport
 * @param level The zones level
 */
fun Client.updateZone(
    xOffset: Int,
    yOffset: Int,
    level: Int
) = send(Protocol.UPDATE_ZONE) {
    writeByteInverse(xOffset)
    writeByteAdd(level)
    writeByteAdd(yOffset)
}