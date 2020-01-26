package org.redrune.network.codec.handshake.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 1:31 a.m.
 */
data class UpdateHandshakeStatus(val opcode: Int) : Message

object StatusCodes {

    const val JS5_RESPONSE_CONNECTED = -2
    const val JS5_RESPONSE_DISCONNECTED = -1
    const val LOGIN_EXCHANGE_KEYS = 0
    const val JS5_RESPONSE_OK = 0
    const val JS5_RESPONSE_CONNECT_OUTOFDATE = 6
    const val JS5_RESPONSE_CONNECT_FULL1 = 7
    const val JS5_RESPONSE_CONNECT_FULL2 = 9

}