package world.gregs.voidps.network.encode

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.GameOpcodes.CLIENT_VARC_STR
import world.gregs.voidps.network.PacketSize
import world.gregs.voidps.network.string
import world.gregs.voidps.network.writeShortAddLittle
import world.gregs.voidps.network.writeString

/**
 * Client variable; also known as "GlobalString"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarcStr(id: Int, value: String) {
    client?.send(CLIENT_VARC_STR, 2 + string(value), PacketSize.SHORT) {
        writeShortAddLittle(id)
        writeString(value)
    }
}