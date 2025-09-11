package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.MINIMAP_STATE
import world.gregs.voidps.network.login.protocol.writeByte

fun Client.sendMinimapState(state: Int) {
    send(MINIMAP_STATE) {
        writeByte(state)
    }
}
