package org.redrune.network.codec.message

import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
abstract class MessageDecoder<T : Message>(val opcodes: IntArray) {

    /**
     * Decoding a [Packet] into a [Message] of type [T]
     */
    abstract fun decode(packet: Packet): T
}