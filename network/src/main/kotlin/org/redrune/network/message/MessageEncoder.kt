package org.redrune.network.message

import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
abstract class MessageEncoder<T : Message> {

    /**
     * Encodes a [Message] of type [T] into a [Packet]
     */
    abstract fun encode(message: T): Packet
}