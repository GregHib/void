package org.redrune.network.packet.codec

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
enum class DecoderState {

    DECODE_OPCODE,

    DECODE_LENGTH,

    DECODE_PAYLOAD
}