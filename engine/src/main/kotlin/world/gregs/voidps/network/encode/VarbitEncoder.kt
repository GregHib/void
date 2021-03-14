package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeIntInverseMiddle
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.GameOpcodes.CLIENT_VARBIT
import world.gregs.voidps.network.GameOpcodes.CLIENT_VARBIT_LARGE

/**
 * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
 * @param id The file id
 * @param value The value to pass to the config file
 */
fun Player.sendVarbit(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        client?.send(CLIENT_VARBIT, 3) {
            writeByteAdd(value)
            writeShortLittle(id)
        }
    } else {
        client?.send(CLIENT_VARBIT_LARGE, 6) {
            writeShortAdd(id)
            writeIntInverseMiddle(value)
        }
    }
}