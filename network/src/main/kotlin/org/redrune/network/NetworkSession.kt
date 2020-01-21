package org.redrune.network

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.AttributeKey
import org.redrune.network.packet.struct.OutgoingPacket
import org.redrune.tools.crypto.IsaacRandomPair

/**
 * This class represents the network session from the client (player) to the server
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class NetworkSession(
        /**
         * The channel for the connection
         */
        var channel: Channel
) {
    /**
     * The current state of the session
     */
    var state: SessionState = SessionState.CREATED

    /**
     * The cipher for incoming packets
     */
    var isaacPair: IsaacRandomPair? = null

    /**
     * Writes a packet to the channel
     * @return ChannelFuture
     */
    fun write(packet: OutgoingPacket, flush: Boolean = true): ChannelFuture {
        return if (flush) channel.writeAndFlush(packet) else channel.write(packet)
    }

    fun inLobby(): Boolean {
        return state == SessionState.LOBBY
    }

    enum class SessionState {
        CREATED, REGISTERED, HANDSHAKE, LOBBY_DECODING, LOBBY, GAME, DEREGISTERED
    }

    companion object {

        /**
         * The attribute that contains the key for a session.
         */
        val SESSION_KEY: AttributeKey<NetworkSession> = AttributeKey.valueOf<NetworkSession>("session.key")
    }
}