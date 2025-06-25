package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.PRIVATE_STATUS
import world.gregs.voidps.network.login.Protocol.PUBLIC_STATUS
import world.gregs.voidps.network.login.protocol.p1Alt1
import world.gregs.voidps.network.login.protocol.p1Alt3

/**
 * @param public (0 = on, 1 = friends, 2 = off, 3 = hide)
 * @param trade (0 = on, 1 = friends, 2 = off)
 */
fun Client.sendPublicStatus(public: Int, trade: Int) {
    return
    send(PUBLIC_STATUS, 2, Client.FIXED) {
        p1Alt3(public)
        p1Alt1(trade)
    }
}

/**
 * @param status (0 = on, 1 = friends, 2 = off)
 */
fun Client.sendPrivateStatus(status: Int) {
    return
    send(PRIVATE_STATUS, 1, Client.FIXED) {
        writeByte(status)
    }
}