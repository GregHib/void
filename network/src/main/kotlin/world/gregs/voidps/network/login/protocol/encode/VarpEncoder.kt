package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CLIENT_VARP
import world.gregs.voidps.network.login.Protocol.CLIENT_VARP_LARGE
import world.gregs.voidps.network.login.protocol.writeByteAdd
import world.gregs.voidps.network.login.protocol.writeIntInverseMiddle
import world.gregs.voidps.network.login.protocol.writeShortAdd

/**
 * A variable player config; also known as "Config", known in the client as "clientvarp"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarp(id: Int, value: Int) {
    return
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARP) {
            writeShort(id)
            writeByteAdd(value)
        }
    } else {
        send(CLIENT_VARP_LARGE) {
            writeIntInverseMiddle(value)
            writeShortAdd(id)
        }
    }
}