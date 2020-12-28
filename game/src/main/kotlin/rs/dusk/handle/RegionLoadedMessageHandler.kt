package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 05, 2020
 */
class RegionLoadedMessageHandler : MessageHandler<RegionLoadedMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: RegionLoadedMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        player.viewport.loaded = true
    }

}