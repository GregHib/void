package org.redrune.network.codec.update

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import org.redrune.network.NetworkHandler
import org.redrune.network.codec.update.message.UpdateMessage
import org.redrune.network.codec.update.message.UpdateStatusCode
import org.redrune.network.codec.update.message.impl.ClientVersionMessage
import org.redrune.network.codec.update.message.impl.FileRequestMessage
import org.redrune.network.codec.update.message.impl.VersionResponseMessage
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
class UpdateHandler : NetworkHandler<UpdateMessage>() {

    private val logger = InlineLogger()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: UpdateMessage) {
        logger.info { "read msg $msg" }
        when (msg) {
            is ClientVersionMessage -> {
                val receivedBuild = msg.majorBuild
                val response = {
                    if (receivedBuild == NetworkConstants.CLIENT_MAJOR_BUILD)
                        UpdateStatusCode.JS5_RESPONSE_OK
                    else
                        UpdateStatusCode.JS5_RESPONSE_CONNECT_OUTOFDATE
                }
                logger.info { "response=${response.invoke()}"}
                val responseMessage =
                    VersionResponseMessage(response.invoke())
                send(ctx, responseMessage)
            }
            is FileRequestMessage -> {
                send(ctx, msg)
            }
        }
    }

}