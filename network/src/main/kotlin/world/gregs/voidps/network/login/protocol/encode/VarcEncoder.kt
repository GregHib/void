package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_LARGE
import world.gregs.voidps.network.login.protocol.writeByteSubtract
import world.gregs.voidps.network.login.protocol.writeIntLittle
import world.gregs.voidps.network.login.protocol.writeShortAddLittle
import world.gregs.voidps.network.login.protocol.writeShortLittle

/**
 * Client variable; also known as "ConfigGlobal"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarc(id: Int, value: Int) {
    return
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARC) {
            writeShortAddLittle(id)
            writeByteSubtract(value)
        }
    } else {
        send(CLIENT_VARC_LARGE) {
            writeShortAddLittle(id)
            writeInt(value)
        }
    }
}