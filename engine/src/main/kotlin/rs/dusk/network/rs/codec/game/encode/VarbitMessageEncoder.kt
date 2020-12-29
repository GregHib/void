package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARBIT
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarbitMessageEncoder : MessageEncoder(CLIENT_VARBIT) {

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
        get<VarbitMessageEncoder>().encode(this, id, value)
    } else {
        get<VarbitLargeMessageEncoder>().encode(this, id, value)
    }
}