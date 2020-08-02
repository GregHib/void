package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_PLAYER_HEAD
import rs.dusk.network.rs.codec.game.encode.message.InterfaceHeadPlayerMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceHeadPlayerMessageEncoder : GameMessageEncoder<InterfaceHeadPlayerMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceHeadPlayerMessage) {
        val (id, component) = msg
        builder.apply {
            writeOpcode(INTERFACE_PLAYER_HEAD)
            writeInt(id shl 16 or component, order = Endian.LITTLE)
        }
    }
}