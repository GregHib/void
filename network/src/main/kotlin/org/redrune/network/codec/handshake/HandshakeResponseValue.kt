package org.redrune.network.codec.handshake

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:54 a.m.
 */
class HandshakeResponseValue {
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