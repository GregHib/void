package world.gregs.voidps.network.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol.MINIMAP_STATE
import world.gregs.voidps.network.client.Client

fun Client.sendMinimapState(state: Int) {
    send(MINIMAP_STATE) {
        writeByte(state)
    }
}