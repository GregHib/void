package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeByteInverse
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes
import world.gregs.voidps.network.GameOpcodes.CHUNK_CLEAR

/**
 * @param xOffset The chunk x coordinate relative to viewport
 * @param yOffset The chunk y coordinate relative to viewport
 * @param plane The chunks plane
 */
fun Client.clearChunk(
    xOffset: Int,
    yOffset: Int,
    plane: Int
) = send(CHUNK_CLEAR, 3) {
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
) = send(GameOpcodes.UPDATE_CHUNK, 3) {
    writeByteInverse(yOffset)
    writeByteAdd(plane)
    writeByteAdd(xOffset)
}