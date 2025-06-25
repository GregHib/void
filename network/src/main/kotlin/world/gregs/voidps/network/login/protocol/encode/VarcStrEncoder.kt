package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.BYTE
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_STR
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_STR_LARGE
import world.gregs.voidps.network.login.protocol.p2Alt2
import world.gregs.voidps.network.login.protocol.p2Alt3
import world.gregs.voidps.network.login.protocol.writeString

/**
 * Client variable; also known as "GlobalString"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarcStr(id: Int, value: String) {
    val size = 2 + string(value)
    if (size in 0..Byte.MAX_VALUE) {
        send(CLIENT_VARC_STR, size, BYTE) {
            p2Alt3(id)
            writeString(value)
        }
    } else {
        send(CLIENT_VARC_STR_LARGE, size, SHORT) {
            writeString(value)
            p2Alt2(id)
        }
    }
}