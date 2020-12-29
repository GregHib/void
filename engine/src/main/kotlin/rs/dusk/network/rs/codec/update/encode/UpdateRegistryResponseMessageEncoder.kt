package rs.dusk.network.rs.codec.update.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRegistryResponseMessageEncoder : MessageEncoder<UpdateRegistryResponse> {

    override fun encode(builder: PacketWriter, msg: UpdateRegistryResponse) {
        builder.writeOpcode(msg.opcode, PacketSize.FIXED)
    }

}