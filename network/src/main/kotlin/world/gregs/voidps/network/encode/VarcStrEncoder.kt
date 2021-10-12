package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.CLIENT_VARC_STR
import world.gregs.voidps.network.Protocol.CLIENT_VARC_STR_LARGE
import world.gregs.voidps.network.writeShortAdd
import world.gregs.voidps.network.writeShortAddLittle
import world.gregs.voidps.network.writeString

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