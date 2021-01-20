package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CLIENT_VARBIT
import world.gregs.void.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarbitEncoder : Encoder(CLIENT_VARBIT) {

    /**
     * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
     * @param id The file id
     * @param value The value to pass to the config file
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(3) {
        writeByte(value, type = Modifier.SUBTRACT)
        writeShort(id, type = Modifier.ADD)
    }
}

fun Player.sendVarbit(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarbitEncoder>().encode(this, id, value)
    } else {
        get<VarbitLargeEncoder>().encode(this, id, value)
    }
}