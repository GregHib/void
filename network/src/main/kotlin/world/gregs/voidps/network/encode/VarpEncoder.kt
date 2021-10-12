package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.CLIENT_VARP
import world.gregs.voidps.network.Protocol.CLIENT_VARP_LARGE
import world.gregs.voidps.network.writeIntInverseMiddle
import world.gregs.voidps.network.writeShortAdd

/**
 * A variable player config; also known as "Config", known in the client as "clientvarp"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarp(id: Int, value: Int) {
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARP) {
            writeShort(id)
            writeByte(value)
        }
    } else {
        send(CLIENT_VARP_LARGE) {
            writeIntInverseMiddle(value)
            writeShortAdd(id)
        }
    }
}