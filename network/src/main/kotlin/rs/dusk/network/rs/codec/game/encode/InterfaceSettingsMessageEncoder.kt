package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_COMPONENT_SETTINGS
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 26, 2020
 */
class InterfaceSettingsMessageEncoder : MessageEncoder<InterfaceSettingsMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceSettingsMessage) {
        val (id, component, fromSlot, toSlot, settings) = msg
        builder.apply {
            writeOpcode(INTERFACE_COMPONENT_SETTINGS)
            writeShort(fromSlot, order = Endian.LITTLE)
            writeInt(id shl 16 or component, Modifier.INVERSE, Endian.MIDDLE)
            writeShort(toSlot, Modifier.ADD)
            writeInt(settings, order = Endian.LITTLE)
        }
    }
}