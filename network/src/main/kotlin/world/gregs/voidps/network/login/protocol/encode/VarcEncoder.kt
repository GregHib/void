package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_LARGE
import world.gregs.voidps.network.login.protocol.writeIntLittle
import world.gregs.voidps.network.login.protocol.writeShortAddLittle
import world.gregs.voidps.network.login.protocol.writeShortLittle

/**
 * Client variable; also known as "ConfigGlobal"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarc(id: Int, value: Int) {
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARC) {
            writeByte(value)
            writeShortAddLittle(id)
        }
    } else {
        send(CLIENT_VARC_LARGE) {
            writeIntLittle(value)
            writeShortLittle(id)
        }
    }
}
