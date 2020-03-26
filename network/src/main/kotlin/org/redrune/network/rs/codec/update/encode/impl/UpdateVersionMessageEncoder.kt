package org.redrune.network.rs.codec.update.encode.impl

import org.redrune.core.network.codec.packet.access.PacketBuilder
import org.redrune.network.rs.codec.update.encode.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.encode.message.UpdateVersionMessage
import org.redrune.utility.constants.FileServerResponseCodes
import org.redrune.utility.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateVersionMessageEncoder : UpdateMessageEncoder<UpdateVersionMessage>() {

    override fun encode(builder: PacketBuilder, msg: UpdateVersionMessage) {
        builder.writeByte(msg.opcode)
        if (msg.opcode == FileServerResponseCodes.JS5_RESPONSE_OK) {
            NetworkConstants.GRAB_SERVER_KEYS.forEach {
                builder.writeInt(it)
            }
        }
    }

}