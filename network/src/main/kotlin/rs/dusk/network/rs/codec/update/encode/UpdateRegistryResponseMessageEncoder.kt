package rs.dusk.network.rs.codec.update.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.update.UpdateMessageEncoder
import rs.dusk.network.rs.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRegistryResponseMessageEncoder : UpdateMessageEncoder<UpdateRegistryResponse>() {

    override fun encode(builder: PacketWriter, msg: UpdateRegistryResponse) {
        builder.writeOpcode(msg.opcode, PacketType.FIXED)
    }

}