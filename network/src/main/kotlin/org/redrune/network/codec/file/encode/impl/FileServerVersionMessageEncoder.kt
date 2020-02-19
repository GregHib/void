package org.redrune.network.codec.file.encode.impl

import org.redrune.network.codec.file.encode.FileServerMessageEncoder
import org.redrune.network.codec.game.encode.GameMessageEncoder
import org.redrune.network.codec.file.encode.message.FileServerVersionMessage
import org.redrune.network.packet.access.PacketBuilder
import org.redrune.tools.constants.FileServerResponseCodes
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerVersionMessageEncoder : FileServerMessageEncoder<FileServerVersionMessage>() {
    override fun encode(builder: PacketBuilder, msg: FileServerVersionMessage) {
        builder.writeByte(msg.opcode)
        if (msg.opcode == FileServerResponseCodes.JS5_RESPONSE_OK) {
            NetworkConstants.GRAB_SERVER_KEYS.forEach {
                builder.writeInt(it)
            }
        }
    }
}