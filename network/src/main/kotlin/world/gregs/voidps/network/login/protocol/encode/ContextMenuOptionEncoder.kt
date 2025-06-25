package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.BYTE
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.PLAYER_OPTION
import world.gregs.voidps.network.login.protocol.*

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
    p1Alt1(top)
    ip2(cursor)
    writeString(option)
    p1Alt2(slot)
}