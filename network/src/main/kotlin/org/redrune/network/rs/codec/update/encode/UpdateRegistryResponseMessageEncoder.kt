package org.redrune.network.rs.codec.update.encode

import org.redrune.core.network.codec.packet.access.PacketWriter
import org.redrune.core.network.model.packet.PacketType
import org.redrune.network.rs.codec.update.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRegistryResponseMessageEncoder : UpdateMessageEncoder<UpdateRegistryResponse>() {

    override fun encode(builder: PacketWriter, msg: UpdateRegistryResponse) {
        builder.writeOpcode(msg.opcode, PacketType.FIXED)
    }

}