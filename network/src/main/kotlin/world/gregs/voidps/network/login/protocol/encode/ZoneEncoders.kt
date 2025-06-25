package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.Protocol.CLEAR_ZONE
import world.gregs.voidps.network.login.protocol.p1Alt1
import world.gregs.voidps.network.login.protocol.p1Alt3

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
    send(CLEAR_ZONE) {
        writeByte(xOffset.toByte())
        p1Alt3(yOffset)
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
    send(Protocol.UPDATE_ZONE) {
        p1Alt1(xOffset)
        writeByte(yOffset.toByte())
        p1Alt3(level)
    }
}