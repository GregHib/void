package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARC_LARGE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcLargeEncoder : Encoder(CLIENT_VARC_LARGE) {

    /**
     * Client variable; also known as "ConfigGlobal"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(6) {
        writeShort(id, order = Endian.LITTLE)
        writeInt(value)
    }
}