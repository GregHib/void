package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.CLIENT_VARBIT
import world.gregs.voidps.network.login.Protocol.CLIENT_VARBIT_LARGE
import world.gregs.voidps.network.login.protocol.writeByteAdd
import world.gregs.voidps.network.login.protocol.writeIntInverseMiddle
import world.gregs.voidps.network.login.protocol.writeShortAdd
import world.gregs.voidps.network.login.protocol.writeShortLittle

/**
 * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
 * @param id The file id
 * @param value The value to pass to the config file
 */
fun Client.sendVarbit(id: Int, value: Int) {
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        send(CLIENT_VARBIT) {
            writeByteAdd(value)
            writeShortLittle(id)
        }
    } else {
        send(CLIENT_VARBIT_LARGE) {
            writeShortAdd(id)
            writeIntInverseMiddle(value)
        }
    }
}
