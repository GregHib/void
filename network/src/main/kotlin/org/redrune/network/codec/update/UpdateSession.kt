package org.redrune.network.codec.update

import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.handler.timeout.ReadTimeoutHandler
import org.redrune.network.Session
import org.redrune.network.codec.update.message.UpdateVersionMessage
import org.redrune.network.codec.update.message.VersionResponseMessage
import org.redrune.network.message.Message
import org.redrune.tools.constants.NetworkConstants


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 12:50 a.m.
 */
class UpdateSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Message) {
        if (msg is UpdateVersionMessage) {
            val valid = msg.version == NetworkConstants.CLIENT_MAJOR_BUILD
            val opcode =
                if (valid) {
                    UpdateStatusCode.JS5_RESPONSE_OK
                } else {
                    UpdateStatusCode.JS5_RESPONSE_CONNECT_OUTOFDATE
                }
            channel.writeAndFlush(VersionResponseMessage(opcode))
            if (valid) {
                channel.pipeline().remove(ReadTimeoutHandler::class.java)
            } else {
                channel.close()
            }
        }
    }
}