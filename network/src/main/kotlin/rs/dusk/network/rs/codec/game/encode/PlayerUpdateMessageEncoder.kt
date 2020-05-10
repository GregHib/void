package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_UPDATING
import rs.dusk.network.rs.codec.game.encode.message.PlayerUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class PlayerUpdateMessageEncoder : GameMessageEncoder<PlayerUpdateMessage>() {

    override fun encode(builder: PacketWriter, msg: PlayerUpdateMessage) {
        val (changes, updates) = msg
        builder.apply {
            writeOpcode(PLAYER_UPDATING, PacketType.SHORT)
            writeBytes(changes.buffer)
            writeBytes(updates.buffer)
        }
        msg.release()
    }
}