package org.redrune.network.model.message

import org.redrune.network.model.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
abstract class MessageEncoder<T: Message> {

    /**
     * Encodes a message and returns the response
     */
    abstract fun encode(out: PacketBuilder, msg: T)
}