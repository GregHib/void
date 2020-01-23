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