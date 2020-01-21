package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
class PacketReader(private val buffer: ByteBuf) {

    constructor(packet: Packet) : this(buffer = packet.buffer)
    constructor(array: ByteArray) : this(buffer = Unpooled.wrappedBuffer(array))



}