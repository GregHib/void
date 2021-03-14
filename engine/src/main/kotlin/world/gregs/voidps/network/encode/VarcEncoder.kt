package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.buffer.write.writeIntLittle
import world.gregs.voidps.buffer.write.writeShortAddLittle
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.GameOpcodes.CLIENT_VARC
import world.gregs.voidps.network.GameOpcodes.CLIENT_VARC_LARGE

/**
 * Client variable; also known as "ConfigGlobal"
 * @param id The config id
 * @param value The value to pass to the config
 */
fun Player.sendVarc(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        client?.send(CLIENT_VARC, 3) {
            writeByte(value)
            writeShortAddLittle(id)
        }
    } else {
        client?.send(CLIENT_VARC_LARGE, 6) {
            writeIntLittle(value)
            writeShortLittle(id)
        }
    }
}