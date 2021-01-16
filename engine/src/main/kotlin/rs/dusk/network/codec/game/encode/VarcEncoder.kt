package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeShort
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.CLIENT_VARC
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcEncoder : Encoder(CLIENT_VARC) {

    /**
     * Client variable; also known as "ConfigGlobal"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(3) {
        writeByte(value)
        writeShort(id, Modifier.ADD, Endian.LITTLE)
    }
}

fun Player.sendVarc(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarcEncoder>().encode(this, id, value)
    } else {
        get<VarcLargeEncoder>().encode(this, id, value)
    }
}