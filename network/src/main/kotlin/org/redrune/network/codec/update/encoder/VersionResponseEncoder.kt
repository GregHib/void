package org.redrune.network.codec.update.encoder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.update.message.UpdateStatusCode
import org.redrune.network.codec.update.message.impl.VersionResponseMessage
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
class VersionResponseEncoder : MessageToByteEncoder<VersionResponseMessage>() {
    override fun encode(ctx: ChannelHandlerContext, msg: VersionResponseMessage, out: ByteBuf) {
        out.writeByte(msg.responseCode)
        if (msg.responseCode == UpdateStatusCode.JS5_RESPONSE_OK) {
            NetworkConstants.GRAB_SERVER_KEYS.forEach {
                out.writeInt(it)
            }
        }
    }
}