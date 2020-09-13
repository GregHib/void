package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_WEIGHT
import rs.dusk.network.rs.codec.game.encode.message.WeightMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since September 13, 2020
 */
class WeightMessageEncoder : GameMessageEncoder<WeightMessage>() {

    override fun encode(builder: PacketWriter, msg: WeightMessage) {
        builder.apply {
            writeOpcode(PLAYER_WEIGHT)
            writeShort(msg.weight)
        }
    }
}