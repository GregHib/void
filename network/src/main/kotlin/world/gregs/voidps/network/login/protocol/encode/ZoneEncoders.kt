package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.Protocol.CLEAR_ZONE
import world.gregs.voidps.network.login.protocol.writeByteAdd
import world.gregs.voidps.network.login.protocol.writeByteInverse
import world.gregs.voidps.network.login.protocol.writeByteSubtract

/**
 * @param xOffset The zone x coordinate relative to viewport
 * @param yOffset The zone y coordinate relative to viewport
 * @param level The zones level
 */
fun Client.clearZone(
    xOffset: Int,
    yOffset: Int,
    level: Int
) {
    return
    send(CLEAR_ZONE) {
        writeByte(xOffset.toByte())
        writeByteSubtract(yOffset)
        writeByte(level.toByte())
    }
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
) {
    return
    send(Protocol.UPDATE_ZONE) {
        writeByteAdd(xOffset)
        writeByte(yOffset.toByte())
        writeByteSubtract(level)
    }
}