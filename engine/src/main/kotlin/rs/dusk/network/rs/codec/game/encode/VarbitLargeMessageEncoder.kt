package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARBIT_LARGE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarbitLargeMessageEncoder : MessageEncoder(CLIENT_VARBIT_LARGE) {

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
        writeInt(value)
        writeShort(id)
    }
}