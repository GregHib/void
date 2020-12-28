package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class WalkMapMessageHandler : MessageHandler<WalkMapMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: WalkMapMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (x, y) = msg
        player.walkTo(player.tile.copy(x = x, y = y)) { result ->
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
        }
    }

}