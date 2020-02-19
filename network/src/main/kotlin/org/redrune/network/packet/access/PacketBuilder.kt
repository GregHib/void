package org.redrune.network.packet.access

import io.netty.buffer.ByteBuf
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * The building of a packet is done by this class
 *
 * @author Greg Hibb
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class PacketBuilder(override val buffer: ByteBuf, override val cipher: IsaacCipher? = null) : PacketWriter() {

    init {
        if (opcode != null) {
            writeOpcode(opcode!!, type)
        }
    }

}