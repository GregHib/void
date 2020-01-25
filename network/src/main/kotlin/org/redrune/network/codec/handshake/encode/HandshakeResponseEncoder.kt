package org.redrune.network.codec.handshake.encode

import org.redrune.network.codec.handshake.UpdateMessageEncoder
import org.redrune.network.codec.handshake.message.HandshakeResponse
import org.redrune.network.codec.handshake.message.HandshakeResponseValue
import org.redrune.network.packet.PacketBuilder
import org.redrune.tools.constants.NetworkConstants.Companion.GRAB_SERVER_KEYS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 1:27 a.m.
 */
class HandshakeResponseEncoder : UpdateMessageEncoder<HandshakeResponse>() {

    override fun encode(buf: PacketBuilder, msg: HandshakeResponse) {
        buf.writeByte(msg.responseValue)
        if (msg.responseValue == HandshakeResponseValue.SUCCESSFUL) {
            GRAB_SERVER_KEYS.forEach { buf.writeInt(it) }
        }
    }


}