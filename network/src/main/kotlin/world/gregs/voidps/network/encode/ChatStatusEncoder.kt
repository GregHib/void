package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.PRIVATE_STATUS
import world.gregs.voidps.network.Protocol.PUBLIC_STATUS
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeByteSubtract

/**
 * @param public (0 = on, 1 = friends, 2 = off, 3 = hide)
 * @param trade (0 = on, 1 = friends, 2 = off)
 */
fun Client.sendPublicStatus(public: Int, trade: Int) {
    send(PUBLIC_STATUS, 2, Client.FIXED) {
        writeByteSubtract(public)
        writeByteAdd(trade)
    }
}

/**
 * @param status (0 = on, 1 = friends, 2 = off)
 */
fun Client.sendPrivateStatus(status: Int) {
    send(PRIVATE_STATUS, 1, Client.FIXED) {
        writeByte(status)
    }
}