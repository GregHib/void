package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARP_LARGE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarpLargeMessageEncoder : MessageEncoder(CLIENT_VARP_LARGE) {

    /**
     * A variable player config; also known as "Config", known in the client as "clientvarp"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(5) {
        writeInt(value, Modifier.INVERSE, Endian.MIDDLE)
        writeShort(id, Modifier.ADD)
    }
}