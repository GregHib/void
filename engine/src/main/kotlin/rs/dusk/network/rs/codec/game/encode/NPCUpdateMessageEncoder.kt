package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_UPDATING
import rs.dusk.network.rs.codec.game.encode.message.NPCUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class NPCUpdateMessageEncoder : MessageEncoder<NPCUpdateMessage> {

    override fun encode(builder: PacketWriter, msg: NPCUpdateMessage) {
        val (changes, updates) = msg
        builder.apply {
            writeOpcode(NPC_UPDATING, PacketType.SHORT)
            writeBytes(changes.buffer)
            writeBytes(updates.buffer)
        }
        msg.release()
    }
}