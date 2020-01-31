package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf

/**
 * This class represents a
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
abstract class Packet(val payload: ByteBuf) {

    /**
     * The initial length of the packet
     */
    val length = payload.readableBytes()
}