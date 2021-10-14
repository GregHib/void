package world.gregs.voidps.network.encode

import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.PLAYER_OPTION

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