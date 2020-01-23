package org.redrune.network.message

import org.redrune.network.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
abstract class MessageDecoder<T : Message>(val opcodes: IntArray) {

    /**
     * Decoding a [PacketReader] into a [Message] of type [T]
     */
    abstract fun decode(packet: PacketReader): T
}