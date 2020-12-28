package rs.dusk.network.rs.codec.game.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.network.rs.codec.game.encode.message.WorldListResponseMessage

class WorldListRefreshMessageHandler : MessageHandler() {

    override fun refreshWorldList(context: ChannelHandlerContext, full: Boolean) {
        context.pipeline().writeAndFlush(WorldListResponseMessage(full))
    }

}