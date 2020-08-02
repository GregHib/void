package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_SPRITE
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSpriteMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceSpriteMessageEncoder : GameMessageEncoder<InterfaceSpriteMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceSpriteMessage) {
        val (id, component, sprite) = msg
        builder.apply {
            writeOpcode(INTERFACE_SPRITE)
            writeShort(sprite, order = Endian.LITTLE)
            writeInt(id shl 16 or component)
        }
    }
}