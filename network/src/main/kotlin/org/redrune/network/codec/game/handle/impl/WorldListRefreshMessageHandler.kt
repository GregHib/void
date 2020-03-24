package org.redrune.network.codec.game.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.game.decode.message.WorldListRefreshMessage
import org.redrune.network.codec.game.encode.message.WorldListResponseMessage
import org.redrune.network.codec.game.handle.GameMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
class WorldListRefreshMessageHandler : GameMessageHandler<WorldListRefreshMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: WorldListRefreshMessage) {
        ctx.pipeline().writeAndFlush(WorldListResponseMessage(msg.crc == 0))
    }
}