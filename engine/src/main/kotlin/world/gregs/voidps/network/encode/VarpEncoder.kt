package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Protocol.CLIENT_VARP
import world.gregs.voidps.network.Protocol.CLIENT_VARP_LARGE
import world.gregs.voidps.network.writeIntInverseMiddle
import world.gregs.voidps.network.writeShortAdd

/**
 * A variable player config; also known as "Config", known in the client as "clientvarp"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarp(id: Int, value: Int) {
    if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        client?.send(CLIENT_VARP, 3) {
            writeShort(id)
            writeByte(value)
        }
    } else {
        client?.send(CLIENT_VARP_LARGE, 6) {
            writeIntInverseMiddle(value)
            writeShortAdd(id)
        }
    }
}