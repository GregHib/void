package org.redrune.network.model.packet.data

import io.netty.buffer.ByteBuf
import org.redrune.network.model.packet.PacketDecoder
import org.redrune.network.model.packet.PacketReader
import org.redrune.network.model.packet.PacketType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class DataPacketDecoder : PacketDecoder() {

    /**
     * Packets are encoded into a [DataPacket]
     */
    override fun constructPacket(opcode: Int, length: Int, type: PacketType, payload: ByteBuf): PacketReader {
        return PacketReader(payload)
    }

    /**
     * All data packets have no opcode
     */
    override fun readOpcode(buf: ByteBuf): Int {
        return -1
    }

    /**
     * The length of data packets is dependent upon the amount of bytes in the buffer
     */
    override fun readLength(buf: ByteBuf): Pair<Int, PacketType> {
        return Pair(buf.readableBytes(), PacketType.RAW)
    }

}