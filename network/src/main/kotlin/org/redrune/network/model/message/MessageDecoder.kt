package org.redrune.network.model.message

import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.PacketReader
import org.redrune.network.model.packet.PacketType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
abstract class MessageDecoder(
    /**
     * The relevant opcodes for this decoder
     */
    // TODO change to vararg int
    val opcodes: IntArray,

    /**
     * The expected packet length of the message being decoded
     */
    val length: Int
) {

    constructor(type: PacketType, opcodes: IntArray) : this(opcodes, type.id)

    /**
     * Decodes a [Packet] into a [Message]
     */
    abstract fun decode(reader: PacketReader): Message
}