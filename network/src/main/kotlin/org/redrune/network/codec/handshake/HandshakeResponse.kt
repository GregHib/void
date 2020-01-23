package org.redrune.network.codec.handshake

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:44 a.m.
 */
class HandshakeResponse(
    /**
     * The state of the handshake response, it will either be successful or out of date
     */
    val state: HandshakeState
)

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:54 a.m.
 */
enum class HandshakeState(
    /**
     * The value of the state that the client recognizes
     */
    val value: Int
) {

    /**
     * Establishes successful client connection between the server and the client.
     */
    SUCCESSFUL(0),

    /**
     * Tells the requested client that it is currently out-of-date to the connected game server.
     */
    OUT_OF_DATE(6)
}