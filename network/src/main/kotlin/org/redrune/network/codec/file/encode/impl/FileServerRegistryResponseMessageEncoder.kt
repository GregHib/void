package org.redrune.network.codec.file.encode.impl

import org.redrune.network.codec.file.encode.FileServerMessageEncoder
import org.redrune.network.codec.game.encode.GameMessageEncoder
import org.redrune.network.codec.file.encode.message.FileServerRegistryResponse
import org.redrune.network.packet.PacketType
import org.redrune.network.packet.access.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerRegistryResponseMessageEncoder : FileServerMessageEncoder<FileServerRegistryResponse>() {
    override fun encode(builder: PacketBuilder, msg: FileServerRegistryResponse) {
        builder.writeOpcode(msg.opcode, PacketType.FIXED)
    }
}