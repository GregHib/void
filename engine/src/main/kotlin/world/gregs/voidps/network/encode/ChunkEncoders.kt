package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.Protocol.CHUNK_CLEAR
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteInverse

/**
 * @param xOffset The chunk x coordinate relative to viewport
 * @param yOffset The chunk y coordinate relative to viewport
 * @param plane The chunks plane
 */
fun Client.clearChunk(
    xOffset: Int,
    yOffset: Int,
    plane: Int
) = send(CHUNK_CLEAR) {
    writeByteAdd(plane)
    writeByteInverse(yOffset)
    writeByteInverse(xOffset)
}

/**
 * @param xOffset The chunk x coordinate relative to viewport
 * @param yOffset The chunk y coordinate relative to viewport
 * @param plane The chunks plane
 */
fun Client.updateChunk(
    xOffset: Int,
    yOffset: Int,
    plane: Int
) = send(Protocol.UPDATE_CHUNK) {
    writeByteInverse(yOffset)
    writeByteAdd(plane)
    writeByteAdd(xOffset)
}