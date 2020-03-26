package org.redrune.network.codec.update.encode.impl

import org.redrune.core.network.model.packet.PacketType
import org.redrune.core.network.model.packet.access.PacketBuilder
import org.redrune.network.codec.update.encode.UpdateMessageEncoder
import org.redrune.network.codec.update.encode.message.UpdateRegistryResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRegistryResponseMessageEncoder : UpdateMessageEncoder<UpdateRegistryResponse>() {

    override fun encode(builder: PacketBuilder, msg: UpdateRegistryResponse) {
        builder.writeOpcode(msg.opcode, PacketType.FIXED)
    }

}