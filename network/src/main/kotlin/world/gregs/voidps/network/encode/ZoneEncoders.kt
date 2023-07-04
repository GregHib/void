package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.Protocol.CLEAR_ZONE
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteInverse

/**
 * @param xOffset The zone x coordinate relative to viewport
 * @param yOffset The zone y coordinate relative to viewport
 * @param plane The zones plane
 */
fun Client.clearZone(
    xOffset: Int,
    yOffset: Int,
    plane: Int
) = send(CLEAR_ZONE) {
    writeByteAdd(plane)
    writeByteInverse(yOffset)
    writeByteInverse(xOffset)
}

/**
 * @param xOffset The zone x coordinate relative to viewport
 * @param yOffset The zone y coordinate relative to viewport
 * @param plane The zones plane
 */
fun Client.updateZone(
    xOffset: Int,
    yOffset: Int,
    plane: Int
) = send(Protocol.UPDATE_ZONE) {
    writeByteInverse(xOffset)
    writeByteAdd(plane)
    writeByteAdd(yOffset)
}