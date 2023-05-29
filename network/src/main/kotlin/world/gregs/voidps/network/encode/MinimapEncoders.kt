package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.MINIMAP_STATE

fun Client.sendMinimapState(state: Int) {
    send(MINIMAP_STATE) {
        writeByte(state)
    }
}