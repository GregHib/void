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
     * The expected packet length of the message being decoded
     */
    val length: Int,

    /**
     * The relevant opcodes for this decoder
     */
    vararg val opcodes: Int
) {

    constructor(type: PacketType, vararg opcodes: Int) : this(type.id, *opcodes)

    /**
     * Decodes a [Packet] into a [Message]
     */
    abstract fun decode(reader: PacketReader): Message
}