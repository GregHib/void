package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC
import world.gregs.voidps.network.login.Protocol.CLIENT_VARC_LARGE
import world.gregs.voidps.network.login.protocol.ip2
import world.gregs.voidps.network.login.protocol.p1Alt3
import world.gregs.voidps.network.login.protocol.p2Alt3

/**
 * Client variable; also known as "ConfigGlobal"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Client.sendVarc(id: Int, value: Int) {
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARC) {
            p2Alt3(id)
            p1Alt3(value)
        }
    } else {
        send(CLIENT_VARC_LARGE) {
            ip2(id)
            writeInt(value)
        }
    }
}