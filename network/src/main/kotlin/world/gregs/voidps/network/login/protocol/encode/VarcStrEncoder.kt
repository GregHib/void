package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.BYTE
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.client.Client.Companion.string
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_STR
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_STR_LARGE
import world.gregs.voidps.network.login.protocol.writeShortAdd
import world.gregs.voidps.network.login.protocol.writeShortAddLittle
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
            writeString(value)
            writeShortAdd(id)
        }
    } else {
        send(CLIENT_VARC_STR_LARGE, size, SHORT) {
            writeShortAddLittle(id)
            writeString(value)
        }
    }
}
