package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeShortAddLittle
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.PLAYER_OPTION
import world.gregs.voidps.network.PacketSize.BYTE
import world.gregs.voidps.network.string

/**
 * Sends a player right click option
 * @param option The option
 * @param slot The index of the option
 * @param top Whether it should be forced to the top?
 * @param cursor Unknown value
 */
fun Client.contextMenuOption(
    option: String?,
    slot: Int,
    top: Boolean,
    cursor: Int = -1
) = send(PLAYER_OPTION, 4 + string(option), BYTE) {
    writeShortAddLittle(cursor)
    writeString(option)
    writeByteSubtract(slot)
    writeByteAdd(top)
}