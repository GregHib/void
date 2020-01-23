package org.redrune.network.session

import io.netty.channel.Channel
import org.redrune.network.codec.handshake.HandshakeRequest
import org.redrune.network.codec.handshake.HandshakeResponse
import org.redrune.network.codec.handshake.HandshakeState
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:25 a.m.
 */
class HandshakeSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Any) {
        val request = msg as HandshakeRequest
        val validBuild = request.majorBuild == NetworkConstants.CLIENT_MAJOR_BUILD
        send(HandshakeResponse(if (validBuild) HandshakeState.SUCCESSFUL else HandshakeState.OUT_OF_DATE))
    }

}