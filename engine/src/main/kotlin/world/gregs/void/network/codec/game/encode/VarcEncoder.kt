package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CLIENT_VARC
import world.gregs.void.utility.get

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
        writeShort(id, Modifier.ADD, Endian.LITTLE)
        writeByte(value, Modifier.SUBTRACT)
    }
}

fun Player.sendVarc(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarcEncoder>().encode(this, id, value)
    } else {
        get<VarcLargeEncoder>().encode(this, id, value)
    }
}