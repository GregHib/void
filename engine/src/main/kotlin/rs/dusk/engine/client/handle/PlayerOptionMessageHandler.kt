package rs.dusk.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PlayerOptionMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.approach

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class PlayerOptionMessageHandler : GameMessageHandler<PlayerOptionMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val players: Players by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PlayerOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (index, option) = msg
        val target = players.getAtIndex(index) ?: return
        player.approach(target) { result ->
            println("Result $result")
        }
    }

}