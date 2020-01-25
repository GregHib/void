package org.redrune.network.codec.handshake.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 1:31 a.m.
 */
data class HandshakeResponse(val responseValue: Int) : Message

class ResponseValue {
    companion object {
        /**
         * Establishes successful client connection between the server and the client.
         */
        const val SUCCESSFUL = 0

        /**
         * Tells the requested client that it is currently out-of-date to the connected game server.
         */
        const val OUT_OF_DATE = 6

        /**
         * Tells the requested client that it is currently out-of-date to the connected game server.
         */
        const val SERVER_FULL = 7
    }
}