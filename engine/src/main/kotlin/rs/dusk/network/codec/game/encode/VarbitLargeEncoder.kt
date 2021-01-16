package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.CLIENT_VARBIT_LARGE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarbitLargeEncoder : Encoder(CLIENT_VARBIT_LARGE) {

    /**
     * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
     * @param id The file id
     * @param value The value to pass to the config file
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(6) {
        writeShort(id, type = Modifier.ADD)
        writeInt(value, Modifier.INVERSE, Endian.MIDDLE)
    }
}