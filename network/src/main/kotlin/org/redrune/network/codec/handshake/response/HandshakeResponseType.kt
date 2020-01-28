package org.redrune.network.codec.handshake.response

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 27, 2020
 */
enum class HandshakeResponseType(val opcode: Int) {

    SUCCESSFUL(0),

    SERVER_FULL(6);
}