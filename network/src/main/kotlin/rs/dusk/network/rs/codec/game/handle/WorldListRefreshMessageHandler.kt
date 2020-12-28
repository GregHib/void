package rs.dusk.network.rs.codec.game.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.network.rs.codec.game.decode.message.WorldListRefreshMessage
import rs.dusk.network.rs.codec.game.encode.message.WorldListResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
class WorldListRefreshMessageHandler : MessageHandler<WorldListRefreshMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: WorldListRefreshMessage) {
        ctx.pipeline().writeAndFlush(WorldListResponseMessage(msg.crc == 0))
    }
}