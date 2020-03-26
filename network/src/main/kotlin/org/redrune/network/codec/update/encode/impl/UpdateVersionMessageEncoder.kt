package org.redrune.network.codec.update.encode.impl

import org.redrune.core.network.model.packet.access.PacketBuilder
import org.redrune.network.codec.update.encode.UpdateMessageEncoder
import org.redrune.network.codec.update.encode.message.UpdateVersionMessage
import org.redrune.tools.constants.FileServerResponseCodes
import org.redrune.tools.constants.NetworkConstants

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