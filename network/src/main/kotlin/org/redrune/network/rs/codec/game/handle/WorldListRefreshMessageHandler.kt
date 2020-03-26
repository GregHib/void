package org.redrune.network.rs.codec.game.handle

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.rs.codec.game.decode.message.WorldListRefreshMessage
import org.redrune.network.rs.codec.game.encode.message.WorldListResponseMessage
import org.redrune.network.rs.codec.game.GameMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
class WorldListRefreshMessageHandler : GameMessageHandler<WorldListRefreshMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: WorldListRefreshMessage) {
        ctx.pipeline().writeAndFlush(WorldListResponseMessage(msg.crc == 0))
    }
}